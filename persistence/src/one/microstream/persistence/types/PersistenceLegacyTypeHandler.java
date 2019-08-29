package one.microstream.persistence.types;

import one.microstream.collections.types.XGettingEnum;
import one.microstream.reflect.XReflect;

public interface PersistenceLegacyTypeHandler<M, T> extends PersistenceTypeHandler<M, T>
{
	@Override
	public default PersistenceLegacyTypeHandler<M, T> initialize(final long typeId)
	{
		if(typeId == this.typeId())
		{
			return this;
		}
		
		// (01.06.2018 TM)NOTE: /!\ copied from PersistenceTypeHandler#initializeTypeId
		// (26.04.2017 TM)EXCP: proper exception
		throw new IllegalArgumentException(
			"Specified type ID " + typeId
			+ " conflicts with already initalized type ID "
			+ this.typeId()
		);
	}

	@Override
	public default void store(final M medium, final T instance, final long objectId, final PersistenceStoreHandler handler)
	{
		// (13.09.2018 TM)EXCP: proper exception
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
	
	
	public static <T, M> T resolveEnumConstant(
		final PersistenceLegacyTypeHandler<M, T> typeHandler,
		final M                                  medium     ,
		final Integer[]                          ordinalMap
	)
	{
		final int     persistedEnumOrdinal = typeHandler.getPersistedEnumOrdinal(medium);
		final Integer mappedOrdinal        = ordinalMap[persistedEnumOrdinal];
		if(mappedOrdinal == null)
		{
			// enum constant intentionally deleted, return null as instance (effectively "deleting" it on load)
			return null;
		}
		
		return XReflect.resolveEnumConstantInstanceTyped(typeHandler.type(), mappedOrdinal.intValue());
	}
	
	
	
	public abstract class Abstract<M, T> implements PersistenceLegacyTypeHandler<M, T>
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

