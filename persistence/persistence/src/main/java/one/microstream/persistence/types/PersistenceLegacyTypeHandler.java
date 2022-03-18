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

import one.microstream.collections.types.XGettingEnum;
import one.microstream.persistence.exceptions.PersistenceException;
import one.microstream.reflect.XReflect;

public interface PersistenceLegacyTypeHandler<D, T> extends PersistenceTypeHandler<D, T>
{
	@Override
	public default PersistenceLegacyTypeHandler<D, T> initialize(final long typeId)
	{
		if(typeId == this.typeId())
		{
			return this;
		}
		
		// (01.06.2018 TM)NOTE: /!\ copied from PersistenceTypeHandler#initializeTypeId
		throw new PersistenceException(
			"Specified type ID " + typeId
			+ " conflicts with already initalized type ID "
			+ this.typeId()
		);
	}

	@Override
	public default void store(
		final D                          data    ,
		final T                          instance,
		final long                       objectId,
		final PersistenceStoreHandler<D> handler
	)
	{
		throw new UnsupportedOperationException(
			PersistenceLegacyTypeHandler.class.getSimpleName()
			+ " for type " + this.toTypeIdentifier()
			+ " may never store anything."
		);
	}
	
	@Override
	public default Object[] collectEnumConstants()
	{
		// indicate discarding of constants root entry during root resolving
		return null;
	}
	
	
	public static <T, D> T resolveEnumConstant(
		final PersistenceLegacyTypeHandler<D, T> typeHandler,
		final D                                  data       ,
		final Integer[]                          ordinalMap
	)
	{
		final int     persistedEnumOrdinal = typeHandler.getPersistedEnumOrdinal(data);
		final Integer mappedOrdinal        = ordinalMap[persistedEnumOrdinal];
		if(mappedOrdinal == null)
		{
			// enum constant intentionally deleted, return null as instance (effectively "deleting" it on load)
			return null;
		}
		
		return XReflect.resolveEnumConstantInstanceTyped(typeHandler.type(), mappedOrdinal.intValue());
	}
	
	
	
	public abstract class Abstract<D, T> implements PersistenceLegacyTypeHandler<D, T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final PersistenceTypeDefinition typeDefinition;
		
		

		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Abstract(final PersistenceTypeDefinition typeDefinition)
		{
			super();
			this.typeDefinition = typeDefinition;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final long typeId()
		{
			return this.typeDefinition.typeId();
		}
		
		@Override
		public final String runtimeTypeName()
		{
			return this.typeDefinition.runtimeTypeName();
		}

		@Override
		public final String typeName()
		{
			return this.typeDefinition.typeName();
		}

		@Override
		public final boolean isPrimitiveType()
		{
			return this.typeDefinition.isPrimitiveType();
		}

		// persisted-form-related methods, so the old type definition has be used //
		
		public PersistenceTypeDefinition legacyTypeDefinition()
		{
			return this.typeDefinition;
		}

		@Override
		public final XGettingEnum<? extends PersistenceTypeDefinitionMember> allMembers()
		{
			return this.typeDefinition.allMembers();
		}

		@Override
		public final XGettingEnum<? extends PersistenceTypeDefinitionMember> instanceMembers()
		{
			return this.typeDefinition.instanceMembers();
		}
		
		@Override
		public final long membersPersistedLengthMinimum()
		{
			return this.typeDefinition.membersPersistedLengthMinimum();
		}
		
		@Override
		public final long membersPersistedLengthMaximum()
		{
			return this.typeDefinition.membersPersistedLengthMaximum();
		}

		@Override
		public final boolean hasPersistedReferences()
		{
			return this.typeDefinition.hasPersistedReferences();
		}

		@Override
		public final boolean hasPersistedVariableLength()
		{
			return this.typeDefinition.hasPersistedVariableLength();
		}

		@Override
		public final boolean hasVaryingPersistedLengthInstances()
		{
			return this.typeDefinition.hasVaryingPersistedLengthInstances();
		}
		
		// end of persisted-form-related methods //
	
	}
	
}

