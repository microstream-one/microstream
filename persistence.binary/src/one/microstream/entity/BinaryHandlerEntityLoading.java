package one.microstream.entity;

import static one.microstream.X.notNull;

import java.util.function.Consumer;

import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryTypeHandler;
import one.microstream.persistence.exceptions.PersistenceException;
import one.microstream.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceStoreHandler;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;
import one.microstream.persistence.types.PersistenceTypeDescriptionMember;
import one.microstream.persistence.types.PersistenceTypeHandler;

public interface BinaryHandlerEntityLoading<T> extends BinaryTypeHandler<T> 
{
	public default void store(
		final Binary                          data    , 
		final T                               instance, 
		final long                            objectId, 
		final PersistenceStoreHandler<Binary> handler
	)
	{
		// (14.04.2020 FH)EXCP: proper exception
		throw new PersistenceException(
			"Only the identity layer of an entity can be persisted."
		);
	}
	
	public BinaryTypeHandler<T> createStoringEntityHandler();
	
	
	public static <T> BinaryHandlerEntityLoading<T> New(final BinaryTypeHandler<T> delegate)
	{
		return new Default<>(
			notNull(delegate)
		);
	}
	
	
	public static class Default<T> implements BinaryHandlerEntityLoading<T>
	{
		final BinaryTypeHandler<T> delegate;

		Default(final BinaryTypeHandler<T> delegate)
		{
			super();
			this.delegate = delegate;
		}

		public PersistenceTypeHandler<Binary, T> initialize(final long typeId)
		{
			this.delegate.initialize(typeId);
			return this;
		}

		public Class<Binary> dataType()
		{
			return this.delegate.dataType();
		}

		public long typeId()
		{
			return this.delegate.typeId();
		}

		public String typeName()
		{
			return this.delegate.typeName();
		}

		public XGettingSequence<? extends PersistenceTypeDescriptionMember> instanceReferenceMembers()
		{
			return this.delegate.instanceReferenceMembers();
		}

		public String runtimeTypeName()
		{
			return this.delegate.runtimeTypeName();
		}

		public Class<T> type()
		{
			return this.delegate.type();
		}

		public boolean isValidEntityType(final Class<? extends T> type)
		{
			return this.delegate.isValidEntityType(type);
		}

		public XGettingSequence<? extends PersistenceTypeDescriptionMember> instancePrimitiveMembers()
		{
			return this.delegate.instancePrimitiveMembers();
		}

		public void validateEntityType(final Class<? extends T> type)
		{
			this.delegate.validateEntityType(type);
		}

		public XGettingEnum<? extends PersistenceTypeDefinitionMember> allMembers()
		{
			return this.delegate.allMembers();
		}

		public XGettingEnum<? extends PersistenceTypeDefinitionMember> instanceMembers()
		{
			return this.delegate.instanceMembers();
		}

		public void iterateInstanceReferences(final T instance, final PersistenceFunction iterator)
		{
			this.delegate.iterateInstanceReferences(instance, iterator);
		}

		public void iterateLoadableReferences(final Binary data, final PersistenceReferenceLoader iterator)
		{
			this.delegate.iterateLoadableReferences(data, iterator);
		}

		public T create(final Binary data, final PersistenceLoadHandler handler)
		{
			return this.delegate.create(data, handler);
		}

		public boolean hasPersistedReferences()
		{
			return this.delegate.hasPersistedReferences();
		}

		public long membersPersistedLengthMinimum()
		{
			return this.delegate.membersPersistedLengthMinimum();
		}

		public String toTypeIdentifier()
		{
			return this.delegate.toTypeIdentifier();
		}

		public void initializeState(final Binary data, final T instance, final PersistenceLoadHandler handler)
		{
			this.delegate.initializeState(data, instance, handler);
		}

		public long membersPersistedLengthMaximum()
		{
			return this.delegate.membersPersistedLengthMaximum();
		}

		public boolean hasPersistedVariableLength()
		{
			return this.delegate.hasPersistedVariableLength();
		}

		public void updateState(final Binary data, final T instance, final PersistenceLoadHandler handler)
		{
			this.delegate.updateState(data, instance, handler);
		}

		public void complete(final Binary data, final T instance, final PersistenceLoadHandler handler)
		{
			this.delegate.complete(data, instance, handler);
		}

		public boolean isPrimitiveType()
		{
			return this.delegate.isPrimitiveType();
		}

		public boolean hasVaryingPersistedLengthInstances()
		{
			return this.delegate.hasVaryingPersistedLengthInstances();
		}

		public <C extends Consumer<? super Class<?>>> C iterateMemberTypes(final C logic)
		{
			return this.delegate.iterateMemberTypes(logic);
		}

		public String toRuntimeTypeIdentifier()
		{
			return this.delegate.toRuntimeTypeIdentifier();
		}

		public XGettingEnum<? extends PersistenceTypeDefinitionMember> membersInDeclaredOrder()
		{
			return this.delegate.membersInDeclaredOrder();
		}

		public XGettingEnum<? extends PersistenceTypeDescriptionMember> storingMembers()
		{
			return this.delegate.storingMembers();
		}

		public XGettingEnum<? extends PersistenceTypeDescriptionMember> settingMembers()
		{
			return this.delegate.settingMembers();
		}

		public void guaranteeSpecificInstanceViablity() throws PersistenceExceptionTypeNotPersistable
		{
			this.delegate.guaranteeSpecificInstanceViablity();
		}

		public boolean isSpecificInstanceViable()
		{
			return this.delegate.isSpecificInstanceViable();
		}

		public void guaranteeSubTypeInstanceViablity() throws PersistenceExceptionTypeNotPersistable
		{
			this.delegate.guaranteeSubTypeInstanceViablity();
		}

		public boolean isSubTypeInstanceViable()
		{
			return this.delegate.isSubTypeInstanceViable();
		}

		public Object[] collectEnumConstants()
		{
			return this.delegate.collectEnumConstants();
		}

		public int getPersistedEnumOrdinal(final Binary data)
		{
			return this.delegate.getPersistedEnumOrdinal(data);
		}
	
		@Override
		public BinaryTypeHandler<T> createStoringEntityHandler()
		{
			return new Storing<>(this.delegate);
		}
		
		
		static class Storing<T> extends Default<T>
		{
			Storing(BinaryTypeHandler<T> delegate)
			{
				super(delegate);
			}
			
			@Override
			public void store(
				final Binary                          data    ,
				final T                               instance,
				final long                            objectId,
				final PersistenceStoreHandler<Binary> handler
			)
			{
				this.delegate.store(data, instance, objectId, handler);
			}
			
		}
		
	}
	
}
