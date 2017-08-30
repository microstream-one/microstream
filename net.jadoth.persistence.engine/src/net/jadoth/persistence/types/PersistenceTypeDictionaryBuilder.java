package net.jadoth.persistence.types;

import static net.jadoth.Jadoth.notNull;

import java.util.function.Function;

import net.jadoth.collections.BulkList;
import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.JadothSort;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.collections.types.XList;
import net.jadoth.collections.types.XTable;
import net.jadoth.util.KeyValue;

public interface PersistenceTypeDictionaryBuilder
{
	public PersistenceTypeDictionary buildTypeDictionary(XGettingSequence<? extends PersistenceTypeDictionaryEntry> entries);
	
	
	
	
	public static PersistenceTypeDictionaryBuilder.Implementation New()
	{
		return new PersistenceTypeDictionaryBuilder.Implementation();
	}
	
	public class Implementation implements PersistenceTypeDictionaryBuilder
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		protected boolean latestTypeDescriptionIsCurrent()
		{
			return true;
		}
		
		protected <T> PersistenceTypeDescriptionLineage<T> createTypeDescriptionFamily(
			final String                                             typeName,
			final XGettingTable<Long, PersistenceTypeDictionaryEntry> members
		)
		{
			PersistenceTypeDescriptionLineage.New(typeName, members, null, null, false)

			
			return currentTypeDescription;
		}
		
		public static XTable<String, ? extends XTable<Long, PersistenceTypeDictionaryEntry>> groupByTypeName(
			final XGettingSequence<? extends PersistenceTypeDictionaryEntry> entries
		)
		{
			final EqHashTable<String, EqHashTable<Long, PersistenceTypeDictionaryEntry>> table    = EqHashTable.New();
			final Function<String, EqHashTable<Long, PersistenceTypeDictionaryEntry>>    supplier = tn -> EqHashTable.New();
			
			for(final PersistenceTypeDictionaryEntry e : entries)
			{
				table.ensure(e.typeName(), supplier).add(e.typeId(), e);
			}
			
			for(final EqHashTable<Long, PersistenceTypeDictionaryEntry> e : table.values())
			{
				JadothSort.valueSort(e.keys(), Long::compare);
			}
			
			return table;
		}
		
		
		@Override
		public PersistenceTypeDictionary buildTypeDictionary(
			final XGettingSequence<? extends PersistenceTypeDictionaryEntry> entries
		)
		{
			final XTable<String, ? extends XTable<Long, PersistenceTypeDictionaryEntry>> table = groupByTypeName(entries);
			
			final EqHashTable<Long, PersistenceTypeDescription<?>> typeDescriptions = EqHashTable.New();
			
			for(final KeyValue<String, ? extends XTable<Long, PersistenceTypeDictionaryEntry>> e : table)
			{
				
				final PersistenceTypeDescriptionLineage<?> td = createTypeDescriptionFamily(
					e.key()          ,
					e.value()
				);
				typeDescriptions.add(td);
			}
			
			return PersistenceTypeDictionary.New(typeDescriptions);
		}
		
	}
	
	public static PersistenceTypeDictionaryBuilder.RuntimeLinker New(
		final PersistenceRuntimeTypeDescriptionProvider  runtimeTypeDescriptionProvider,
		final PersistenceTypeDescription.Builder         typeDescriptionBuilder        ,
		final PersistenceTypeDescriptionMismatchListener typeMismatchListener
	)
	{
		return new RuntimeLinker(
			notNull(runtimeTypeDescriptionProvider),
			notNull(typeDescriptionBuilder)        ,
			notNull(typeMismatchListener)
		);
	}
	
	public final class RuntimeLinker extends PersistenceTypeDictionaryBuilder.Implementation
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final PersistenceRuntimeTypeDescriptionProvider  runtimeTypeDescriptionProvider;
		final PersistenceTypeDescription.Builder         typeDescriptionBuilder        ;
		final PersistenceTypeDescriptionMismatchListener typeMismatchListener          ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		RuntimeLinker(
			final PersistenceRuntimeTypeDescriptionProvider  runtimeTypeDescriptionProvider,
			final PersistenceTypeDescription.Builder         typeDescriptionBuilder        ,
			final PersistenceTypeDescriptionMismatchListener typeMismatchListener
		)
		{
			super();
			this.runtimeTypeDescriptionProvider = runtimeTypeDescriptionProvider;
			this.typeDescriptionBuilder         = typeDescriptionBuilder        ;
			this.typeMismatchListener           = typeMismatchListener          ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		protected <T> PersistenceTypeDescriptionLineage<T> createTypeDescriptionFamily(
			final String                                typeName,
			final XList<PersistenceTypeDictionaryEntry> members
		)
		{
			final PersistenceTypeDescription<T> latestTypeDescription = super.createTypeDescriptionFamily(
				typeName,
				typeFamily,
				obsoletes
			);
						
			final PersistenceTypeDescription<T> runtimeTypeDescription =
				this.runtimeTypeDescriptionProvider.provideRuntimeTypeDescription(
					latestTypeDescription,
					obsoletes,
					(latest, runtime) ->
					{
						obsoletes.add(latest.typeId(), latest);
						this.typeMismatchListener.registerMismatch(latest, runtime);
					}
				)
			;
			
			return runtimeTypeDescription;
		}
		
	}
	
}
