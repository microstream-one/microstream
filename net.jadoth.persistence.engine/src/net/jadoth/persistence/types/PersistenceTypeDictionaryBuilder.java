package net.jadoth.persistence.types;

import static net.jadoth.Jadoth.notNull;

import java.util.Comparator;
import java.util.function.Function;

import net.jadoth.collections.BulkList;
import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.JadothSort;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.collections.types.XList;
import net.jadoth.collections.types.XTable;
import net.jadoth.swizzling.types.SwizzleTypeIdOwner;
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
		
		protected <T> PersistenceTypeDescription<T> createTypeDescriptionFamily(
			final String                                      typeName  ,
			final XList<PersistenceTypeDictionaryEntry>       typeFamily,
			final XTable<Long, PersistenceTypeDescription<T>> obsoletes
		)
		{
			final PersistenceTypeDictionaryEntry current = typeFamily.pop();
			
			final PersistenceTypeDescription<T> currentTypeDescription = PersistenceTypeDescription.New(
				current.typeId()                     ,
				current.typeName()                   ,
				null                                 ,
				current.members()                    ,
				null                                 ,
				this.latestTypeDescriptionIsCurrent(),
				true                                 ,
				obsoletes
			);
			
			for(final PersistenceTypeDictionaryEntry e : typeFamily)
			{
				final PersistenceTypeDescription<T> td = PersistenceTypeDescription.New(
					e.typeId()            ,
					e.typeName()          ,
					null                  ,
					e.members()           ,
					currentTypeDescription,
					false                 ,
					false                 ,
					obsoletes
				);
				obsoletes.add(td.typeId(), td);
			}
			
			return currentTypeDescription;
		}
		
		public static XTable<String, ? extends XList<PersistenceTypeDictionaryEntry>> groupByTypeName(
			final XGettingSequence<? extends PersistenceTypeDictionaryEntry> entries
		)
		{
			final EqHashTable<String, BulkList<PersistenceTypeDictionaryEntry>> table    = EqHashTable.New();
			final Function<String, BulkList<PersistenceTypeDictionaryEntry>>    supplier = tn -> BulkList.New();
			
			for(final PersistenceTypeDictionaryEntry e : entries)
			{
				table.ensure(e.typeName(), supplier).add(e);
			}
			
			final Comparator<PersistenceTypeDictionaryEntry> orderByTypeIdAscending = SwizzleTypeIdOwner::orderAscending;
			for(final BulkList<PersistenceTypeDictionaryEntry> e : table.values())
			{
				JadothSort.valueSort(e, orderByTypeIdAscending);
			}
			
			return table;
		}
		
		
		@Override
		public PersistenceTypeDictionary buildTypeDictionary(
			final XGettingSequence<? extends PersistenceTypeDictionaryEntry> entries
		)
		{
			final XTable<String, ? extends XList<PersistenceTypeDictionaryEntry>> table = groupByTypeName(entries);
			
			final BulkList<PersistenceTypeDescription<?>> typeDescriptions = BulkList.New(table.size());
			
			for(final KeyValue<String, ? extends XList<PersistenceTypeDictionaryEntry>> e : table)
			{
				final PersistenceTypeDescription<?> td = createTypeDescriptionFamily(
					e.key()          ,
					e.value()        ,
					EqHashTable.New()
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
		protected <T> PersistenceTypeDescription<T> createTypeDescriptionFamily(
			final String                                      typeName  ,
			final XList<PersistenceTypeDictionaryEntry>       typeFamily,
			final XTable<Long, PersistenceTypeDescription<T>> obsoletes
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
