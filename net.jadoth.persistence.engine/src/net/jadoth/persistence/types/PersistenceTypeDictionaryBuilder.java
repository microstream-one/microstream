package net.jadoth.persistence.types;

import static net.jadoth.Jadoth.notNull;

import java.util.function.Function;

import net.jadoth.collections.BulkList;
import net.jadoth.collections.EqHashEnum;
import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.JadothSort;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.collections.types.XTable;
import net.jadoth.util.KeyValue;


@FunctionalInterface
public interface PersistenceTypeDictionaryBuilder
{
	public PersistenceTypeDictionary buildTypeDictionary(XGettingSequence<? extends PersistenceTypeDictionaryEntry> entries);
		
	
	
	public static XTable<String, ? extends XTable<Long, PersistenceTypeDictionaryEntry>> groupEntries(
		final XGettingSequence<? extends PersistenceTypeDictionaryEntry> entries
	)
	{
		// validate TypeId uniqueness accross all entries.
		validateTypeIdUniqueness(entries);
		
		final EqHashTable<String, EqHashTable<Long, PersistenceTypeDictionaryEntry>> table    = EqHashTable.New();
		final Function<String, EqHashTable<Long, PersistenceTypeDictionaryEntry>>    supplier = tn -> EqHashTable.New();
		
		for(final PersistenceTypeDictionaryEntry e : entries)
		{
			// TypeId uniqueness is guaranteed by the validation above.
			table.ensure(e.typeName(), supplier).add(e.typeId(), e);
		}
		
		return table;
	}
	
	public static void validateTypeIdUniqueness(final XGettingSequence<? extends PersistenceTypeDictionaryEntry> entries)
	{
		final EqHashEnum<Long> uniqueTypeIds = EqHashEnum.New(entries.size());
		for(final PersistenceTypeDictionaryEntry e : entries)
		{
			if(!uniqueTypeIds.add(e.typeId()))
			{
				// (05.09.2017 TM)EXCP: proper exception
				throw new RuntimeException("Duplicate TypeDictionary entry for TypeId " + e.typeId());
			}
		}
		
		// no TypeId conflict found, return without consequence.
	}
	
	public static XTable<String, ? extends XTable<Long, PersistenceTypeDictionaryEntry>> sortByTypeId(
		final XTable<String, ? extends XTable<Long, PersistenceTypeDictionaryEntry>> table
	)
	{
		for(final XTable<Long, PersistenceTypeDictionaryEntry> e : table.values())
		{
			JadothSort.valueSort(e.keys(), Long::compare);
		}
		
		return table;
	}
	
	public static void populateTypeLineage(
		final PersistenceTypeLineage<?>                          typeLineage,
		final XGettingCollection<PersistenceTypeDictionaryEntry> entries
	)
	{
		for(final PersistenceTypeDictionaryEntry e : entries)
		{
			typeLineage.registerTypeDescription(e.typeId(), e.members());
		}
	}
	
	public static PersistenceTypeDictionary buildTypeDictionary(
		final PersistenceTypeLineageBuilder                              typeLineageBuilder,
		final XGettingSequence<? extends PersistenceTypeDictionaryEntry> entries
	)
	{
		final BulkList<PersistenceTypeLineage<?>> dictionaryTypeLineages = BulkList.New();

		fillTypeLineages(typeLineageBuilder, dictionaryTypeLineages, entries);
		
		return PersistenceTypeDictionary.New(typeLineageBuilder, dictionaryTypeLineages);
	}
	
	public static void fillTypeLineages(
		final PersistenceTypeLineageBuilder                              typeLineageBuilder    ,
		final BulkList<PersistenceTypeLineage<?>>                        dictionaryTypeLineages,
		final XGettingSequence<? extends PersistenceTypeDictionaryEntry> entries
	)
	{
		if(entries == null)
		{
			// lineages collection remains empty
			return;
		}
		
		final XTable<String, ? extends XTable<Long, PersistenceTypeDictionaryEntry>> table = groupEntries(entries);

		// this sorting is required by the type checking in order to (easily) get the entry with the highest typeId.
		sortByTypeId(table);
		
		for(final KeyValue<String, ? extends XTable<Long, PersistenceTypeDictionaryEntry>> e : table)
		{
			final PersistenceTypeLineage<?> typeLineage = typeLineageBuilder.buildTypeLineage(e.key());
			populateTypeLineage(typeLineage, e.value().values());
			dictionaryTypeLineages.add(typeLineage);
		}
	}
	
	
	
	public static PersistenceTypeDictionaryBuilder.Implementation New(
		final PersistenceTypeLineageBuilder typeLineageBuilder
	)
	{
		return new PersistenceTypeDictionaryBuilder.Implementation(
			notNull(typeLineageBuilder)
		);
	}
	
	public class Implementation implements PersistenceTypeDictionaryBuilder
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final PersistenceTypeLineageBuilder typeLineageBuilder;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final PersistenceTypeLineageBuilder typeLineageBuilder
		)
		{
			super();
			this.typeLineageBuilder = typeLineageBuilder;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public PersistenceTypeDictionary buildTypeDictionary(
			final XGettingSequence<? extends PersistenceTypeDictionaryEntry> entries
		)
		{
			/* (29.09.2017 TM)NOTE:
			 * This is what clean code should look like:
			 * - the interface defining the behavior.
			 * - the implementation holding data / references and choosing which logic to use with the data.
			 * - the actual logic modularized into static methods to be reusable for other implementations.
			 * Also:
			 * - small methods without nested loops to better support JITting.
			 * - properly named methods and variables.
			 * - explanatory comments where naming isn't self-explanatory.
			 */
			
			return PersistenceTypeDictionaryBuilder.buildTypeDictionary(this.typeLineageBuilder, entries);
		}
				
	}
	
}
