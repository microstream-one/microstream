package net.jadoth.persistence.types;

import static net.jadoth.X.notNull;

import net.jadoth.collections.BulkList;
import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.XSort;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.collections.types.XGettingTable;


@FunctionalInterface
public interface PersistenceTypeDictionaryBuilder
{
	public PersistenceTypeDictionary buildTypeDictionary(XGettingSequence<? extends PersistenceTypeDictionaryEntry> entries);
		
	
	
	public static XGettingTable<Long, PersistenceTypeDictionaryEntry> ensureUniqueTypeIds(
		final XGettingSequence<? extends PersistenceTypeDictionaryEntry> entries
	)
	{
		final EqHashTable<Long, PersistenceTypeDictionaryEntry> uniqueTypeIdEntries = EqHashTable.New();
		
		// entries may be null, e.g. when there is no imported type dictionary, yet.
		if(entries != null)
		{
			for(final PersistenceTypeDictionaryEntry e : entries)
			{
				if(!uniqueTypeIdEntries.add(e.typeId(), e))
				{
					// (12.10.2017 TM)EXCP: proper exception
					throw new RuntimeException("TypeId conflict for " + e.typeId() + " " + e.typeName());
				}
			}
			XSort.valueSort(uniqueTypeIdEntries.keys(), Long::compare);
		}
		
		return uniqueTypeIdEntries;
	}
	
	public static PersistenceTypeDictionary buildTypeDictionary(
		final PersistenceTypeDictionaryCreator                           typeDictionaryCreator,
		final PersistenceTypeDefinitionCreator                           typeDefinitionCreator,
		final XGettingSequence<? extends PersistenceTypeDictionaryEntry> entries
	)
	{
		final XGettingTable<Long, PersistenceTypeDictionaryEntry> uniqueTypeIdEntries = ensureUniqueTypeIds(entries);
		
		final BulkList<PersistenceTypeDefinition<?>> typeDefs = BulkList.New(uniqueTypeIdEntries.size());
		for(final PersistenceTypeDictionaryEntry e : uniqueTypeIdEntries.values())
		{
			/* Notes:
			 * 
			 * Type dictionary entries do not necessarily have to be resolvable to the current type model.
			 * 
			 * The type entry just contains all member entries as they are written in the dictionary,
			 * even if they are inconsistent (e.g. duplicates). The point where unvalidated entries are formed into
			 * valid definitions is exactely here, so here has to be the validation.
			 */
			/* (05.10.2018 TM)FIXME: OGS-3: use RefactoringMapping here
			 * Also here is the point where a refactoring mapping must be used to lookup mapping and potentially
			 * transform type names.
			 * Currently, it is used much later and just for the simple case of unresolvable types.
			 * But consider the following case:
			 * Class A is part of the dictionary and has persisted instances.
			 * During the developement process, it gets renamed to B and a new Class is created with the name A.
			 * Design-wise, the entry saying "A" must now be mapped to the type "B".
			 * The name "A" could still be resolved to a valid runtime class, but it would be the wrong one.
			 */
			final Class<?>                     type    = Persistence.resolveTypeOptional(e.typeName());
			final PersistenceTypeDefinition<?> typeDef = typeDefinitionCreator.createTypeDefinition(
				e.typeName(),
				type        ,
				e.typeId()  ,
				PersistenceTypeDescriptionMember.validateAndImmure(e.members())
			);
			typeDefs.add(typeDef);
		}

		// collected type definitions are bulk-registered for efficiency reasons (only sort once)
		final PersistenceTypeDictionary typeDictionary = typeDictionaryCreator.createTypeDictionary();
		typeDictionary.registerTypeDefinitions(typeDefs);
				
		return typeDictionary;
	}
	
	
	
	public static PersistenceTypeDictionaryBuilder.Implementation New(
		final PersistenceTypeDictionaryCreator typeDictionaryCreator,
		final PersistenceTypeDefinitionCreator typeDefinitionCreator
	)
	{
		return new PersistenceTypeDictionaryBuilder.Implementation(
			notNull(typeDictionaryCreator),
			notNull(typeDefinitionCreator)
		);
	}
	
	public class Implementation implements PersistenceTypeDictionaryBuilder
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final PersistenceTypeDictionaryCreator typeDictionaryCreator;
		final PersistenceTypeDefinitionCreator typeDefinitionCreator;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final PersistenceTypeDictionaryCreator typeDictionaryCreator,
			final PersistenceTypeDefinitionCreator typeDefinitionCreator
		)
		{
			super();
			this.typeDictionaryCreator = typeDictionaryCreator;
			this.typeDefinitionCreator = typeDefinitionCreator;
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
			return PersistenceTypeDictionaryBuilder.buildTypeDictionary(
				this.typeDictionaryCreator,
				this.typeDefinitionCreator ,
				entries
			);
		}
				
	}
	
}
