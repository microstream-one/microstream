package one.microstream.persistence.types;

import static one.microstream.X.notNull;

import java.util.function.Consumer;

import one.microstream.collections.types.XGettingEnum;
import one.microstream.persistence.exceptions.PersistenceExceptionTypeNotPersistable;

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
	
	
	public static <M, T> PersistenceLegacyTypeHandler<M, T> Wrap(
		final PersistenceTypeDefinition    legacyTypeDefinition,
		final PersistenceTypeHandler<M, T> currentTypeHandler
	)
	{
		return new Wrapper<>(
			notNull(legacyTypeDefinition),
			notNull(currentTypeHandler)
		);
	}
	
	public final class Wrapper<M, T> extends PersistenceLegacyTypeHandler.Abstract<M, T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final PersistenceTypeHandler<M, T> typeHandler;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Wrapper(
			final PersistenceTypeDefinition    typeDefinition,
			final PersistenceTypeHandler<M, T> typeHandler
		)
		{
			super(typeDefinition);
			this.typeHandler = typeHandler;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public boolean hasInstanceReferences()
		{
			return this.typeHandler.hasInstanceReferences();
		}

		@Override
		public void iterateInstanceReferences(final T instance, final PersistenceFunction iterator)
		{
			this.typeHandler.iterateInstanceReferences(instance, iterator);
		}

		@Override
		public void iterateLoadableReferences(final M medium, final PersistenceObjectIdAcceptor iterator)
		{
			// current type handler perfectly fits the old types structure, so it can be used here.
			this.typeHandler.iterateLoadableReferences(medium, iterator);
		}

		@Override
		public T create(final M medium, final PersistenceObjectIdResolver idResolver)
		{
			return this.typeHandler.create(medium, idResolver);
		}

		@Override
		public void update(final M medium, final T instance, final PersistenceObjectIdResolver idResolver)
		{
			this.typeHandler.update(medium, instance, idResolver);
		}

		@Override
		public void complete(final M medium, final T instance, final PersistenceObjectIdResolver idResolver)
		{
			this.typeHandler.complete(medium, instance, idResolver);
		}
		
		@Override
		public <C extends Consumer<? super Class<?>>> C iterateMemberTypes(final C logic)
		{
			return this.typeHandler.iterateMemberTypes(logic);
		}
		
		@Override
		public final Class<T> type()
		{
			return this.typeHandler.type();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// default method implementations //
		///////////////////////////////////
		
		/*
		 * Tricky:
		 * Must pass through all default methods to be a correct wrapper.
		 * Otherwise, the wrapper changes the behavior in an unwanted fashion.
		 */
		
		@Override
		public XGettingEnum<? extends PersistenceTypeDefinitionMember> membersInDeclaredOrder()
		{
			// Must pass through all default methods to be a correct wrapper.
			return this.typeHandler.membersInDeclaredOrder();
		}
		
		@Override
		public void guaranteeSpecificInstanceViablity() throws PersistenceExceptionTypeNotPersistable
		{
			// Must pass through all default methods to be a correct wrapper.
			this.typeHandler.guaranteeSpecificInstanceViablity();
		}
		
		@Override
		public boolean isSpecificInstanceViable()
		{
			// Must pass through all default methods to be a correct wrapper.
			return this.typeHandler.isSpecificInstanceViable();
		}
		
		@Override
		public void guaranteeSubTypeInstanceViablity() throws PersistenceExceptionTypeNotPersistable
		{
			// Must pass through all default methods to be a correct wrapper.
			this.typeHandler.guaranteeSubTypeInstanceViablity();
		}
		
		@Override
		public boolean isSubTypeInstanceViable()
		{
			// Must pass through all default methods to be a correct wrapper.
			return this.typeHandler.isSubTypeInstanceViable();
		}
		
		@Override
		public Object[] collectEnumConstants()
		{
			// Must pass through all default methods to be a correct wrapper.
			return this.typeHandler.collectEnumConstants();
		}
		
	}

}

