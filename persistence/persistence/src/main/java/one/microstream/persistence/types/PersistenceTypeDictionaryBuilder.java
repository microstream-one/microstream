package one.microstream.persistence.types;

/*-
 * #%L
 * microstream-persistence
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import static one.microstream.X.notNull;

import one.microstream.collections.BulkList;
import one.microstream.collections.EqHashEnum;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.XSort;
import one.microstream.collections.types.XAddingSequence;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XGettingTable;
import one.microstream.persistence.exceptions.PersistenceException;


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
					throw new PersistenceException("TypeId conflict for " + e.typeId() + " " + e.typeName());
				}
			}
			XSort.valueSort(uniqueTypeIdEntries.keys(), Long::compare);
		}
		
		return uniqueTypeIdEntries;
	}
		
	public static PersistenceTypeDictionary buildTypeDictionary(
		final PersistenceTypeDictionaryCreator                           typeDictionaryCreator,
		final PersistenceTypeDefinitionCreator                           typeDefinitionCreator,
		final PersistenceTypeDescriptionResolver                             typeResolver,
		final XGettingSequence<? extends PersistenceTypeDictionaryEntry> entries
	)
	{
		final XGettingTable<Long, PersistenceTypeDictionaryEntry> uniqueTypeIdEntries = ensureUniqueTypeIds(entries);
		
		final PersistenceTypeDefinitionMemberCreator memberCreator =
			PersistenceTypeDefinitionMemberCreator.New(uniqueTypeIdEntries.values(), typeResolver)
		;
						
		final BulkList<PersistenceTypeDefinition> typeDefs = BulkList.New(uniqueTypeIdEntries.size());
		for(final PersistenceTypeDescription e : uniqueTypeIdEntries.values())
		{
			/*
			 * The type entry just contains all member entries as they are written in the dictionary,
			 * even if they are inconsistent (e.g. duplicates) or no longer resolvable to a runtime type.
			 * The point where unvalidated entries are formed into valid definitions is exactely here,
			 * so here has to be the validation and type mapping.
			 */
			/*
			 * The type resolver must also handle refactoring mappings internally.
			 * Not just mapping types with unresolvably deprecated names to their currently named runtime type,
			 * but also rerouting conflicted name changes.
			 * Consider the following case:
			 * Class A is part of the dictionary.
			 * During the developement process, it gets renamed to "B" and a new Class is created with the name "A".
			 * Design-wise, the entry saying "A" must now be mapped to the type B.
			 * Without refactoring mapping, the name "A" could still be resolved to a valid runtime class,
			 * but it would be the wrong one.
			 */
			

			final EqHashEnum<PersistenceTypeDefinitionMember> allMembers =
				EqHashEnum.New(PersistenceTypeDescriptionMember.identityHashEqualator())
			;
			final EqHashEnum<PersistenceTypeDefinitionMember> instanceMembers =
				EqHashEnum.New(PersistenceTypeDescriptionMember.identityHashEqualator())
			;
			
			buildDefinitionMembers(memberCreator, e, allMembers, instanceMembers);
			
			final String   runtimeTypeName = typeResolver.resolveRuntimeTypeName(e);
			final Class<?> type            = runtimeTypeName == null
				? null
				: typeResolver.tryResolveType(runtimeTypeName)
			;
			
			final PersistenceTypeDefinition typeDef = typeDefinitionCreator.createTypeDefinition(
				e.typeId()     ,
				e.typeName()   ,
				runtimeTypeName,
				type           ,
				allMembers     ,
				instanceMembers
			);
			
			typeDefs.add(typeDef);
		}

		// collected type definitions are bulk-registered for efficiency reasons (only sort once)
		final PersistenceTypeDictionary typeDictionary = typeDictionaryCreator.createTypeDictionary();
		typeDictionary.registerTypeDefinitions(typeDefs);
				
		return typeDictionary;
	}
	
	public static void buildDefinitionMembers(
		final PersistenceTypeDefinitionMemberCreator                   memberCreator  ,
		final PersistenceTypeDescription                               typeDescription,
		final XAddingSequence<? super PersistenceTypeDefinitionMember> allMembers     ,
		final XAddingSequence<? super PersistenceTypeDefinitionMember> instanceMembers
	)
	{
		for(final PersistenceTypeDescriptionMember member : typeDescription.allMembers())
		{
			final PersistenceTypeDefinitionMember definitionMember = member.createDefinitionMember(memberCreator);
			if(!allMembers.add(definitionMember))
			{
				throw new PersistenceException("Duplicate type member entry: " + member.identifier());
			}
			if(definitionMember.isInstanceMember())
			{
				instanceMembers.add(definitionMember);
			}
		}
	}
	
	
	
	public static PersistenceTypeDictionaryBuilder.Default New(
		final PersistenceTypeDictionaryCreator typeDictionaryCreator,
		final PersistenceTypeDefinitionCreator typeDefinitionCreator,
		final PersistenceTypeDescriptionResolverProvider  typeResolverProvider
	)
	{
		return new PersistenceTypeDictionaryBuilder.Default(
			notNull(typeDictionaryCreator),
			notNull(typeDefinitionCreator),
			notNull(typeResolverProvider)
		);
	}
	
	public class Default implements PersistenceTypeDictionaryBuilder
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final PersistenceTypeDictionaryCreator typeDictionaryCreator;
		final PersistenceTypeDefinitionCreator typeDefinitionCreator;
		final PersistenceTypeDescriptionResolverProvider typeResolverProvider ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final PersistenceTypeDictionaryCreator typeDictionaryCreator,
			final PersistenceTypeDefinitionCreator typeDefinitionCreator,
			final PersistenceTypeDescriptionResolverProvider  typeResolverProvider
		)
		{
			super();
			this.typeDictionaryCreator = typeDictionaryCreator;
			this.typeDefinitionCreator = typeDefinitionCreator;
			this.typeResolverProvider  = typeResolverProvider ;
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
				this.typeDictionaryCreator                 ,
				this.typeDefinitionCreator                 ,
				this.typeResolverProvider.provideTypeDescriptionResolver(),
				entries
			);
		}
				
	}
	
}
