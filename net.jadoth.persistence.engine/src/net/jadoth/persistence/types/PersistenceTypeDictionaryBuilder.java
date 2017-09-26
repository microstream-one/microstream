package net.jadoth.persistence.types;

import static net.jadoth.Jadoth.notNull;

import java.util.function.Function;

import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.JadothSort;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.collections.types.XTable;
import net.jadoth.util.KeyValue;

public interface PersistenceTypeDictionaryBuilder
{
	public PersistenceTypeDictionary buildTypeDictionary(XGettingSequence<? extends PersistenceTypeDictionaryEntry> entries);
		
	
	
	public static PersistenceTypeDictionaryBuilder.Implementation New(
		final PersistenceTypeDescriptionLineageProvider typeLineageProvider   ,
		final PersistenceTypeDescriptionBuilder         typeDescriptionBuilder
	)
	{
		return new PersistenceTypeDictionaryBuilder.Implementation(
			notNull(typeLineageProvider)   ,
			notNull(typeDescriptionBuilder)
		);
	}
	
	public class Implementation implements PersistenceTypeDictionaryBuilder
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final PersistenceTypeDescriptionLineageProvider typeLineageProvider   ;
		final PersistenceTypeDescriptionBuilder         typeDescriptionBuilder;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final PersistenceTypeDescriptionLineageProvider typeLineageProvider   ,
			final PersistenceTypeDescriptionBuilder         typeDescriptionBuilder
		)
		{
			super();
			this.typeLineageProvider    = typeLineageProvider   ;
			this.typeDescriptionBuilder = typeDescriptionBuilder;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		// (25.09.2017 TM)XXX: remove if not needed
//		protected boolean latestTypeDescriptionIsCurrent()
//		{
//			return true;
//		}
//
//		protected <T> PersistenceTypeDescriptionLineage<T> fillTypeDescriptionLineage(final String typeName)
//		{
//			// no runtime type in default implementation (e.g. standalone process)
//			return PersistenceTypeDescriptionLineage.New(typeName, null);
//		}
		
		public static XTable<String, ? extends XTable<Long, PersistenceTypeDictionaryEntry>> groupAndSort(
			final XGettingSequence<? extends PersistenceTypeDictionaryEntry> entries
		)
		{
			final EqHashTable<String, EqHashTable<Long, PersistenceTypeDictionaryEntry>> table    = EqHashTable.New();
			final Function<String, EqHashTable<Long, PersistenceTypeDictionaryEntry>>    supplier = tn -> EqHashTable.New();
			
			for(final PersistenceTypeDictionaryEntry e : entries)
			{
				if(!table.ensure(e.typeName(), supplier).add(e.typeId(), e))
				{
					// (05.09.2017 TM)EXCP: proper exception
					throw new RuntimeException("Duplicate TypeDictionary entry for TypeId " + e.typeId());
				}
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
			final PersistenceTypeDictionary dictionary = PersistenceTypeDictionary.New(this.typeLineageProvider);

			final XTable<String, ? extends XTable<Long, PersistenceTypeDictionaryEntry>> table = groupAndSort(entries);
			for(final KeyValue<String, ? extends XTable<Long, PersistenceTypeDictionaryEntry>> e : table)
			{
				populateTypeLineage(dictionary.ensureTypeLineage(e.key()), e.value().values());
			}
			
			return dictionary;
		}
		
		private static <T> void populateTypeLineage(
			final PersistenceTypeLineage<T>               typeLineage,
			final XGettingCollection<PersistenceTypeDictionaryEntry> entries
		)
		{
			for(final PersistenceTypeDictionaryEntry e : entries)
			{
				final PersistenceTypeDescription<T> td = PersistenceTypeDescription.New(
					typeLineage,
					e.typeId() ,
					e.members()
				);
				typeLineage.register(td);
			}
		}
		
	}
	
}
