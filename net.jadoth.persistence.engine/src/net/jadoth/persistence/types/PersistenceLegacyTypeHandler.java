package net.jadoth.persistence.types;

import net.jadoth.collections.types.XGettingEnum;

public interface PersistenceLegacyTypeHandler<M, T> extends PersistenceTypeHandler<M, T>
{
	@Override
	public default PersistenceTypeHandler<M, T> initializeTypeId(final long typeId)
	{
		if(typeId == this.typeId())
		{
			return this;
		}
		
		// (01.06.2018 TM)NOTE: /!\ copied from PersistenceTypeHandler#initializeTypeId
		// (26.04.2017 TM)EXCP: proper exception
		throw new IllegalArgumentException(
			"Specified type ID " + typeId + " conflicts with already initalized type ID " + this.typeId()
		);
	}
	
	
	
	public abstract class AbstractImplementation<M, T> implements PersistenceLegacyTypeHandler<M, T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final PersistenceTypeDefinition<T> typeDefinition;
		
		

		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected AbstractImplementation(final PersistenceTypeDefinition<T> typeDefinition)
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
		public final String typeName()
		{
			return this.typeDefinition.typeName();
		}

		@Override
		public final XGettingEnum<? extends PersistenceTypeDescriptionMember> members()
		{
			return this.typeDefinition.members();
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
		public final boolean isPrimitiveType()
		{
			return this.typeDefinition.isPrimitiveType();
		}

		@Override
		public final boolean hasVaryingPersistedLengthInstances()
		{
			return this.typeDefinition.hasVaryingPersistedLengthInstances();
		}

		@Override
		public final Class<T> type()
		{
			// should always be null. Otherwise, something's fishy ...
			return this.typeDefinition.type();
		}
		
	}

}
