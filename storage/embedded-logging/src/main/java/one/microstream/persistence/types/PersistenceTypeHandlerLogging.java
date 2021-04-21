package one.microstream.persistence.types;

import static one.microstream.X.notNull;

import java.util.function.Consumer;

import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.persistence.exceptions.PersistenceExceptionTypeNotPersistable;

public interface PersistenceTypeHandlerLogging<D, T>
	extends PersistenceTypeHandler<D, T>, PersistenceLoggingWrapper<PersistenceTypeHandler<D, T>>
{
	public static <D, T> PersistenceTypeHandlerLogging<D, ? super T> New(
		final PersistenceTypeHandler<D, T> wrapped
	)
	{
		return new Default<>(notNull(wrapped));
	}


	public static class Default<D, T>
		extends PersistenceLoggingWrapper.Abstract<PersistenceTypeHandler<D, T>>
		implements PersistenceTypeHandlerLogging<D, T>
	{
		protected Default(
			final PersistenceTypeHandler<D, T> wrapped
		)
		{
			super(wrapped);
		}

		@Override
		public void store(
			final D                          data    ,
			final T                          instance,
			final long                       objectId,
			final PersistenceStoreHandler<D> storer
		)
		{
			this.logger().persistenceTypeHandler_beforeStore(this, data, instance, objectId, storer);

			this.wrapped().store(data, instance, objectId, storer);

			this.logger().persistenceTypeHandler_afterStore(this, data, instance, objectId, storer);
		}

		@Override
		public void complete(final D data, final T instance, final PersistenceLoadHandler loader)
		{
			this.logger().persistenceTypeHandler_beforeComplete(this, data, instance, loader);

			this.wrapped().complete(data, instance, loader);

			this.logger().persistenceTypeHandler_afterComplete(this, data, instance, loader);
		}

		@Override
		public long typeId()
		{
			return this.wrapped().typeId();
		}

		@Override
		public String typeName()
		{
			return this.wrapped().typeName();
		}

		@Override
		public XGettingSequence<? extends PersistenceTypeDescriptionMember> instanceReferenceMembers()
		{
			return this.wrapped().instanceReferenceMembers();
		}

		@Override
		public String runtimeTypeName()
		{
			return this.wrapped().runtimeTypeName();
		}

		@Override
		public Class<D> dataType()
		{
			return this.wrapped().dataType();
		}

		@Override
		public Class<T> type()
		{
			return this.wrapped().type();
		}

		@Override
		public boolean isValidEntityType(final Class<? extends T> type)
		{
			return this.wrapped().isValidEntityType(type);
		}

		@Override
		public XGettingSequence<? extends PersistenceTypeDescriptionMember> instancePrimitiveMembers()
		{
			return this.wrapped().instancePrimitiveMembers();
		}

		@Override
		public void validateEntityType(final Class<? extends T> type)
		{
			this.wrapped().validateEntityType(type);
		}

		@Override
		public XGettingEnum<? extends PersistenceTypeDefinitionMember> allMembers()
		{
			return this.wrapped().allMembers();
		}

		@Override
		public XGettingEnum<? extends PersistenceTypeDefinitionMember> instanceMembers()
		{
			return this.wrapped().instanceMembers();
		}

		@Override
		public void iterateInstanceReferences(final T instance, final PersistenceFunction iterator)
		{
			this.wrapped().iterateInstanceReferences(instance, iterator);
		}

		@Override
		public void iterateLoadableReferences(final D data, final PersistenceReferenceLoader iterator)
		{
			this.wrapped().iterateLoadableReferences(data, iterator);
		}

		@Override
		public boolean hasPersistedReferences()
		{
			return this.wrapped().hasPersistedReferences();
		}

		@Override
		public long membersPersistedLengthMinimum()
		{
			return this.wrapped().membersPersistedLengthMinimum();
		}

		@Override
		public String toTypeIdentifier()
		{
			return this.wrapped().toTypeIdentifier();
		}

		@Override
		public T create(final D data, final PersistenceLoadHandler handler)
		{
			return this.wrapped().create(data, handler);
		}

		@Override
		public long membersPersistedLengthMaximum()
		{
			return this.wrapped().membersPersistedLengthMaximum();
		}

		@Override
		public void initializeState(final D data, final T instance, final PersistenceLoadHandler handler)
		{
			this.wrapped().initializeState(data, instance, handler);
		}

		@Override
		public boolean hasPersistedVariableLength()
		{
			return this.wrapped().hasPersistedVariableLength();
		}

		@Override
		public void updateState(final D data, final T instance, final PersistenceLoadHandler handler)
		{
			this.wrapped().updateState(data, instance, handler);
		}

		@Override
		public boolean isPrimitiveType()
		{
			return this.wrapped().isPrimitiveType();
		}

		@Override
		public boolean hasVaryingPersistedLengthInstances()
		{
			return this.wrapped().hasVaryingPersistedLengthInstances();
		}

		@Override
		public PersistenceTypeHandler<D, T> initialize(final long typeId)
		{
			return this.wrapped().initialize(typeId);
		}

		@Override
		public <C extends Consumer<? super Class<?>>> C iterateMemberTypes(final C logic)
		{
			return this.wrapped().iterateMemberTypes(logic);
		}

		@Override
		public String toRuntimeTypeIdentifier()
		{
			return this.wrapped().toRuntimeTypeIdentifier();
		}

		@Override
		public XGettingEnum<? extends PersistenceTypeDefinitionMember> membersInDeclaredOrder()
		{
			return this.wrapped().membersInDeclaredOrder();
		}

		@Override
		public XGettingEnum<? extends PersistenceTypeDescriptionMember> storingMembers()
		{
			return this.wrapped().storingMembers();
		}

		@Override
		public XGettingEnum<? extends PersistenceTypeDescriptionMember> settingMembers()
		{
			return this.wrapped().settingMembers();
		}

		@Override
		public void guaranteeSpecificInstanceViablity() throws PersistenceExceptionTypeNotPersistable
		{
			this.wrapped().guaranteeSpecificInstanceViablity();
		}

		@Override
		public boolean isSpecificInstanceViable()
		{
			return this.wrapped().isSpecificInstanceViable();
		}

		@Override
		public void guaranteeSubTypeInstanceViablity() throws PersistenceExceptionTypeNotPersistable
		{
			this.wrapped().guaranteeSubTypeInstanceViablity();
		}

		@Override
		public boolean isSubTypeInstanceViable()
		{
			return this.wrapped().isSubTypeInstanceViable();
		}

		@Override
		public Object[] collectEnumConstants()
		{
			return this.wrapped().collectEnumConstants();
		}

		@Override
		public int getPersistedEnumOrdinal(final D data)
		{
			return this.wrapped().getPersistedEnumOrdinal(data);
		}

	}

}
