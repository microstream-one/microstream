package one.microstream.persistence.binary.util;

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

import one.microstream.collections.HashEnum;
import one.microstream.collections.types.XEnum;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryLoader;
import one.microstream.persistence.binary.types.BinaryPersistenceFoundation;
import one.microstream.persistence.binary.types.BinaryStorer;
import one.microstream.persistence.types.PersistenceContextDispatcher;
import one.microstream.persistence.types.PersistenceLoader;
import one.microstream.persistence.types.PersistenceManager;
import one.microstream.persistence.types.PersistenceObjectIdProvider;
import one.microstream.persistence.types.PersistenceRootsProvider;
import one.microstream.persistence.types.PersistenceStorer;
import one.microstream.persistence.types.PersistenceStorer.CreationObserver;
import one.microstream.persistence.types.PersistenceStorer.Creator;
import one.microstream.persistence.types.PersistenceTypeDictionaryManager;
import one.microstream.persistence.types.PersistenceTypeHandlerManager;
import one.microstream.persistence.types.PersistenceTypeIdProvider;

/**
 * This type serves as a factory instance for building {@link Serializer} instances.
 * However, it is more than a mere factory as it keeps track of all component instances used in building
 * a {@link Serializer} instance. For example managing parts of an application can use it
 * to access former set ID providers or dictionary providers even after they have been assembled into (and
 * are intentionally hidden in) a {@link PersistenceManager} instance.
 * Hence it can be seen as a kind of "master instance" of the built persistence layer or as its "foundation".
 *
 * @param <F> the foundation type
 */
public interface SerializerFoundation<F extends SerializerFoundation<?>> extends BinaryPersistenceFoundation<F>
{
	@Override
	public SerializerFoundation<F> Clone();
	
	public SerializerTypeInfoStrategyCreator getSerializerTypeInfoStrategyCreator();

	public F setSerializerTypeInfoStrategyCreator(SerializerTypeInfoStrategyCreator serializerTypeInfoStrategyCreator);
	
	public XEnum<Class<?>> getEntityTypes();
	
	public F setEntityTypes(XEnum<Class<?>> entityTypes);
	
	public boolean registerEntityType(Class<?> entityType);
	
	public F registerEntityTypes(Class<?>... entityTypes);
	
	public F registerEntityTypes(final Iterable<Class<?>> entityTypes);
	
	
	public static SerializerFoundation<?> New()
	{
		return new SerializerFoundation.Default<>();
	}
	
	
	public static class Default<F extends SerializerFoundation.Default<?>>
	extends BinaryPersistenceFoundation.Default<F>
	implements SerializerFoundation<F>
	{
		private XEnum<Class<?>>                   entityTypes                      ;
		private SerializerTypeInfoStrategyCreator serializerTypeInfoStrategyCreator;
		
		Default()
		{
			super();
		}
		
		@Override
		public SerializerTypeInfoStrategyCreator getSerializerTypeInfoStrategyCreator()
		{
			if(this.serializerTypeInfoStrategyCreator == null)
			{
				this.serializerTypeInfoStrategyCreator = this.ensureSerializerTypeInfoStrategyCreator();
			}
			return this.serializerTypeInfoStrategyCreator;
		}
		
		@Override
		public F setSerializerTypeInfoStrategyCreator(final SerializerTypeInfoStrategyCreator serializerTypeInfoStrategyCreator)
		{
			this.serializerTypeInfoStrategyCreator = serializerTypeInfoStrategyCreator;
			return this.$();
		}
	
		@Override
		public XEnum<Class<?>> getEntityTypes()
		{
			if(this.entityTypes == null)
			{
				this.entityTypes = this.ensureEntityTypes();
			}

			return this.entityTypes;
		}
		
		@Override
		public F setEntityTypes(final XEnum<Class<?>> entityTypes)
		{
			this.entityTypes = entityTypes;
			return this.$();
		}
		
		@Override
		public boolean registerEntityType(final Class<?> entityType)
		{
			return this.getEntityTypes().add(entityType);
		}
		
		@Override
		public F registerEntityTypes(final Class<?>... entityTypes)
		{
			this.getEntityTypes().addAll(entityTypes);
			
			return this.$();
		}
		
		@Override
		public F registerEntityTypes(final Iterable<Class<?>> entityTypes)
		{
			final XEnum<Class<?>> registeredEntityTypes = this.getEntityTypes();
			
			for(final Class<?> entityType : entityTypes)
			{
				registeredEntityTypes.add(entityType);
			}
			
			return this.$();
		}
		
		
		@Override
		public F setObjectIdProvider(final PersistenceObjectIdProvider oidProvider)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public F setTypeIdProvider(final PersistenceTypeIdProvider tidProvider)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public F setContextDispatcher(final PersistenceContextDispatcher<Binary> contextDispatcher)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public F setRootsProvider(final PersistenceRootsProvider<Binary> rootsProvider)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public F setStorerCreationObserver(final CreationObserver liveStorerRegistry)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public F setStorerCreator(final Creator<Binary> storerCreator)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public F setBuilderCreator(final PersistenceLoader.Creator<Binary> builderCreator)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public F setTypeDictionaryManager(final PersistenceTypeDictionaryManager typeDictionaryManager)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public SerializerFoundation.Default<F> Clone()
		{
			return new SerializerFoundation.Default<>();
		}
		
		@Override
		public PersistenceManager<Binary> createPersistenceManager()
		{
			final PersistenceTypeHandlerManager<Binary> typeHandlerManager = this.getTypeHandlerManager();
			typeHandlerManager.initialize();
			this.getEntityTypes().forEach(typeHandlerManager::ensureTypeHandler);
						
			return super.createPersistenceManager();
		}
		
		
		protected XEnum<Class<?>> ensureEntityTypes()
		{
			return HashEnum.New();
		}

		protected SerializerTypeInfoStrategyCreator ensureSerializerTypeInfoStrategyCreator()
		{
			return new SerializerTypeInfoStrategyCreator.TypeDictionary(false);
		}
		
		@Override
		protected PersistenceObjectIdProvider ensureObjectIdProvider()
		{
			return PersistenceObjectIdProvider.Transient();
		}
		
		@Override
		protected PersistenceTypeIdProvider ensureTypeIdProvider()
		{
			return PersistenceTypeIdProvider.Transient();
		}
		
		@Override
		protected PersistenceContextDispatcher<Binary> ensureContextDispatcher()
		{
			return PersistenceContextDispatcher.LocalObjectRegistration();
		}
		
		@Override
		protected PersistenceRootsProvider<Binary> ensureRootsProviderInternal()
		{
			return PersistenceRootsProvider.Empty();
		}
		
		@Override
		protected CreationObserver ensureStorerCreationObserver()
		{
			return PersistenceStorer.CreationObserver::noOp;
		}
		
		@Override
		protected BinaryStorer.Creator ensureStorerCreator()
		{
			return BinaryStorer.Creator(
				() -> 1,
				this.isByteOrderMismatch()
			);
		}

		@Override
		protected BinaryLoader.Creator ensureBuilderCreator()
		{
			return BinaryLoader.CreatorSimple(
				this.isByteOrderMismatch()
			);
		}
		
		@Override
		protected PersistenceTypeDictionaryManager ensureTypeDictionaryManager()
		{
			final PersistenceTypeDictionaryManager newTypeDictionaryManager =
				PersistenceTypeDictionaryManager.Transient(
					this.getTypeDictionaryCreator()
				)
			;
			return newTypeDictionaryManager;
		}
		
	}
	
}
