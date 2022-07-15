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

import java.util.Iterator;

import one.microstream.collections.EqHashEnum;
import one.microstream.collections.XUtilsCollection;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XGettingTable;
import one.microstream.util.similarity.Similarity;


public interface PersistenceLegacyTypeMappingResult<D, T>
{
	// the legacy type might potentially or usually be another type, maybe one that no more has a runtime type.
	public PersistenceTypeDefinition legacyTypeDefinition();
	
	public PersistenceTypeHandler<D, T> currentTypeHandler();
	
	public XGettingTable<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>> legacyToCurrentMembers();
	
	public XGettingTable<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>> currentToLegacyMembers();

	public XGettingEnum<PersistenceTypeDefinitionMember> discardedLegacyMembers();
	
	public XGettingEnum<PersistenceTypeDefinitionMember> newCurrentMembers();
	
	
	
	public static boolean isUnchangedInstanceStructure(
		final PersistenceLegacyTypeMappingResult<?, ?> mappingResult
	)
	{
		return isUnchangedStructure(
			mappingResult.legacyTypeDefinition().instanceMembers(),
			mappingResult.currentTypeHandler().instanceMembers(),
			mappingResult
		);
	}
	
	public static boolean isUnchangedFullStructure(
		final PersistenceLegacyTypeMappingResult<?, ?> mappingResult
	)
	{
		return isUnchangedStructure(
			mappingResult.legacyTypeDefinition().allMembers(),
			mappingResult.currentTypeHandler().allMembers(),
			mappingResult
		);
	}
	
	public static boolean isUnchangedStaticStructure(
		final PersistenceLegacyTypeMappingResult<?, ?> mappingResult
	)
	{
		final EqHashEnum<PersistenceTypeDefinitionMember> legacyEnumMembers = XUtilsCollection.subtract(
			EqHashEnum.<PersistenceTypeDefinitionMember>New(mappingResult.legacyTypeDefinition().allMembers()),
			mappingResult.legacyTypeDefinition().instanceMembers()
		);
		
		final EqHashEnum<PersistenceTypeDefinitionMember> currentEnumMembers = XUtilsCollection.subtract(
			EqHashEnum.<PersistenceTypeDefinitionMember>New(mappingResult.currentTypeHandler().allMembers()),
			mappingResult.currentTypeHandler().instanceMembers()
		);
		
		return PersistenceLegacyTypeMappingResult.isUnchangedStructure(
			legacyEnumMembers,
			currentEnumMembers,
			mappingResult
		);
	}
	
	public static boolean isUnchangedStructure(
		final XGettingEnum<? extends PersistenceTypeDefinitionMember> legacyMembers ,
		final XGettingEnum<? extends PersistenceTypeDefinitionMember> currentMembers,
		final PersistenceLegacyTypeMappingResult<?, ?>                mappingResult
	)
	{
		if(legacyMembers.size() != currentMembers.size())
		{
			// if there are differing members counts, the structure cannot be unchanged.
			return false;
		}

		final Iterator<? extends PersistenceTypeDefinitionMember> legacy  = legacyMembers.iterator();
		final Iterator<? extends PersistenceTypeDefinitionMember> current = currentMembers.iterator();
		
		final XGettingTable<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>> mapping =
			mappingResult.legacyToCurrentMembers()
		;
		
		// check as long as both collections have order-wise corresponding entries (ensured by size check above)
		while(legacy.hasNext())
		{
			final PersistenceTypeDefinitionMember legacyMember  = legacy.next() ;
			final PersistenceTypeDefinitionMember currentMember = current.next();
			
			// all legacy members must be directly mapped to their order-wise corresponding current member.
			if(mapping.get(legacyMember) != currentMember)
			{
				return false;
			}
			
			// and the types must be the same, of course. Member names are sound and smoke.
			if(!legacyMember.typeName().equals(currentMember.typeName()))
			{
				return false;
			}
		}
		
		// no need to check for remaining elements since size was checked above
		return true;
	}
	
	
	
	public static <D, T> PersistenceLegacyTypeMappingResult<D, T> New(
		final PersistenceTypeDefinition                                                                   legacyTypeDefinition  ,
		final PersistenceTypeHandler<D, T>                                                                currentTypeHandler    ,
		final XGettingTable<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>> legacyToCurrentMembers,
		final XGettingTable<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>> currentToLegacyMembers,
		final XGettingEnum<PersistenceTypeDefinitionMember>                                               discardedLegacyMembers,
		final XGettingEnum<PersistenceTypeDefinitionMember>                                               newCurrentMembers
	)
	{
		return new PersistenceLegacyTypeMappingResult.Default<>(
			notNull(legacyTypeDefinition)  ,
			notNull(currentTypeHandler)    ,
			notNull(legacyToCurrentMembers),
			notNull(currentToLegacyMembers),
			notNull(discardedLegacyMembers),
			notNull(newCurrentMembers)
		);
	}
	
	public final class Default<D, T> implements PersistenceLegacyTypeMappingResult<D, T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final PersistenceTypeDefinition                     legacyTypeDefinition  ;
		final PersistenceTypeHandler<D, T>                  currentTypeHandler    ;
		final XGettingEnum<PersistenceTypeDefinitionMember> discardedLegacyMembers;
		final XGettingEnum<PersistenceTypeDefinitionMember> newCurrentMembers     ;
		
		final XGettingTable<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>>
			legacyToCurrentMembers,
			currentToLegacyMembers
		;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final PersistenceTypeDefinition                                                                   legacyTypeDefinition  ,
			final PersistenceTypeHandler<D, T>                                                                currentTypeHandler    ,
			final XGettingTable<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>> legacyToCurrentMembers,
			final XGettingTable<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>> currentToLegacyMembers,
			final XGettingEnum<PersistenceTypeDefinitionMember>                                               discardedLegacyMembers,
			final XGettingEnum<PersistenceTypeDefinitionMember>                                               newCurrentMembers
		)
		{
			super();
			this.legacyTypeDefinition   = legacyTypeDefinition  ;
			this.currentTypeHandler     = currentTypeHandler    ;
			this.legacyToCurrentMembers = legacyToCurrentMembers;
			this.currentToLegacyMembers = currentToLegacyMembers;
			this.discardedLegacyMembers = discardedLegacyMembers;
			this.newCurrentMembers      = newCurrentMembers     ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public PersistenceTypeDefinition legacyTypeDefinition()
		{
			return this.legacyTypeDefinition;
		}

		@Override
		public PersistenceTypeHandler<D, T> currentTypeHandler()
		{
			return this.currentTypeHandler;
		}

		@Override
		public XGettingTable<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>> legacyToCurrentMembers()
		{
			return this.legacyToCurrentMembers;
		}
		
		@Override
		public XGettingTable<PersistenceTypeDefinitionMember, Similarity<PersistenceTypeDefinitionMember>> currentToLegacyMembers()
		{
			return this.currentToLegacyMembers;
		}
		
		@Override
		public XGettingEnum<PersistenceTypeDefinitionMember> discardedLegacyMembers()
		{
			return this.discardedLegacyMembers;
		}

		@Override
		public XGettingEnum<PersistenceTypeDefinitionMember> newCurrentMembers()
		{
			return this.newCurrentMembers;
		}
		
	}
	
}
