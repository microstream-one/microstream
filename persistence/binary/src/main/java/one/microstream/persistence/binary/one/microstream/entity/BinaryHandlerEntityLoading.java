package one.microstream.persistence.binary.one.microstream.entity;

/*-
 * #%L
 * microstream-persistence-binary
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

import java.util.function.Consumer;

import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.persistence.binary.exceptions.BinaryPersistenceException;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryTypeHandler;
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
	@Override
	public default void store(
		final Binary                          data    ,
		final T                               instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		throw new BinaryPersistenceException(
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

		@Override
		public PersistenceTypeHandler<Binary, T> initialize(final long typeId)
		{
			this.delegate.initialize(typeId);
			return this;
		}

		@Override
		public Class<Binary> dataType()
		{
			return this.delegate.dataType();
		}

		@Override
		public long typeId()
		{
			return this.delegate.typeId();
		}

		@Override
		public String typeName()
		{
			return this.delegate.typeName();
		}

		@Override
		public XGettingSequence<? extends PersistenceTypeDescriptionMember> instanceReferenceMembers()
		{
			return this.delegate.instanceReferenceMembers();
		}

		@Override
		public String runtimeTypeName()
		{
			return this.delegate.runtimeTypeName();
		}

		@Override
		public Class<T> type()
		{
			return this.delegate.type();
		}

		@Override
		public boolean isValidEntityType(final Class<? extends T> type)
		{
			return this.delegate.isValidEntityType(type);
		}

		@Override
		public XGettingSequence<? extends PersistenceTypeDescriptionMember> instancePrimitiveMembers()
		{
			return this.delegate.instancePrimitiveMembers();
		}

		@Override
		public void validateEntityType(final Class<? extends T> type)
		{
			this.delegate.validateEntityType(type);
		}

		@Override
		public XGettingEnum<? extends PersistenceTypeDefinitionMember> allMembers()
		{
			return this.delegate.allMembers();
		}

		@Override
		public XGettingEnum<? extends PersistenceTypeDefinitionMember> instanceMembers()
		{
			return this.delegate.instanceMembers();
		}

		@Override
		public void iterateInstanceReferences(final T instance, final PersistenceFunction iterator)
		{
			this.delegate.iterateInstanceReferences(instance, iterator);
		}

		@Override
		public void iterateLoadableReferences(final Binary data, final PersistenceReferenceLoader iterator)
		{
			this.delegate.iterateLoadableReferences(data, iterator);
		}

		@Override
		public T create(final Binary data, final PersistenceLoadHandler handler)
		{
			return this.delegate.create(data, handler);
		}

		@Override
		public boolean hasPersistedReferences()
		{
			return this.delegate.hasPersistedReferences();
		}

		@Override
		public long membersPersistedLengthMinimum()
		{
			return this.delegate.membersPersistedLengthMinimum();
		}

		@Override
		public String toTypeIdentifier()
		{
			return this.delegate.toTypeIdentifier();
		}

		@Override
		public void initializeState(final Binary data, final T instance, final PersistenceLoadHandler handler)
		{
			this.delegate.initializeState(data, instance, handler);
		}

		@Override
		public long membersPersistedLengthMaximum()
		{
			return this.delegate.membersPersistedLengthMaximum();
		}

		@Override
		public boolean hasPersistedVariableLength()
		{
			return this.delegate.hasPersistedVariableLength();
		}

		@Override
		public void updateState(final Binary data, final T instance, final PersistenceLoadHandler handler)
		{
			this.delegate.updateState(data, instance, handler);
		}

		@Override
		public void complete(final Binary data, final T instance, final PersistenceLoadHandler handler)
		{
			this.delegate.complete(data, instance, handler);
		}

		@Override
		public boolean isPrimitiveType()
		{
			return this.delegate.isPrimitiveType();
		}

		@Override
		public boolean hasVaryingPersistedLengthInstances()
		{
			return this.delegate.hasVaryingPersistedLengthInstances();
		}

		@Override
		public <C extends Consumer<? super Class<?>>> C iterateMemberTypes(final C logic)
		{
			return this.delegate.iterateMemberTypes(logic);
		}

		@Override
		public String toRuntimeTypeIdentifier()
		{
			return this.delegate.toRuntimeTypeIdentifier();
		}

		@Override
		public XGettingEnum<? extends PersistenceTypeDefinitionMember> membersInDeclaredOrder()
		{
			return this.delegate.membersInDeclaredOrder();
		}

		@Override
		public XGettingEnum<? extends PersistenceTypeDescriptionMember> storingMembers()
		{
			return this.delegate.storingMembers();
		}

		@Override
		public XGettingEnum<? extends PersistenceTypeDescriptionMember> settingMembers()
		{
			return this.delegate.settingMembers();
		}

		@Override
		public void guaranteeSpecificInstanceViablity() throws PersistenceExceptionTypeNotPersistable
		{
			this.delegate.guaranteeSpecificInstanceViablity();
		}

		@Override
		public boolean isSpecificInstanceViable()
		{
			return this.delegate.isSpecificInstanceViable();
		}

		@Override
		public void guaranteeSubTypeInstanceViablity() throws PersistenceExceptionTypeNotPersistable
		{
			this.delegate.guaranteeSubTypeInstanceViablity();
		}

		@Override
		public boolean isSubTypeInstanceViable()
		{
			return this.delegate.isSubTypeInstanceViable();
		}

		@Override
		public Object[] collectEnumConstants()
		{
			return this.delegate.collectEnumConstants();
		}

		@Override
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
			Storing(final BinaryTypeHandler<T> delegate)
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
