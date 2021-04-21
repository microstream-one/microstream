package one.microstream.persistence.types;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import one.microstream.logging.types.Logger;
import one.microstream.logging.types.LoggerFactory;
import one.microstream.typing.KeyValue;

public interface PersistenceLogger
{
	
	public void persistenceStorer_beforeStore(PersistenceStorer storer, Object instance);

	public void persistenceStorer_afterStore(PersistenceStorer storer, Object instance, long objectId);

	public void persistenceStorer_beforeCommit(PersistenceStorer storer);

	public void persistenceStorer_afterCommit(PersistenceStorer storer, Object status);
	
	public void persistenceStorer_afterRegisterLazy(long objectId, Object instance);
	
	public void persistenceStorer_afterRegisterEager(long objectId, Object instance);
	
	
	public void persistenceLoader_beforeLoadRoots();

	public void persistenceLoader_afterLoadRoots(PersistenceRoots loadedRoots);

	public void persistenceLoader_beforeRegisterSkip(long objectId);

	public void persistenceLoader_afterRegisterSkip(long objectId);

	public void persistenceLoader_beforeGet();

	public void persistenceLoader_afterGet(Object retrieved);

	public void persistenceLoader_beforeGetObject(long objectId);

	public void persistenceLoader_afterGetObject(long objectId, Object retrieved);

	public <C> void persistenceLoader_beforeCollect(C collector, long[] objectIds);

	public <C> void persistenceLoader_afterCollect(C collected);
	

	public <D, T> void persistenceTypeHandler_beforeStore(PersistenceTypeHandler<D, T> typeHandler, D data, T instance, long objectId, PersistenceStoreHandler<D> storer);

	public <D, T> void persistenceTypeHandler_afterStore(PersistenceTypeHandler<D, T> typeHandler, D data, T instance, long objectId, PersistenceStoreHandler<D> storer);

	public <D, T> void persistenceTypeHandler_beforeComplete(PersistenceTypeHandler<D, T> typeHandler, D data, T instance, PersistenceLoadHandler loader);

	public <D, T> void persistenceTypeHandler_afterComplete(PersistenceTypeHandler<D, T> typeHandler, D data, T instance, PersistenceLoadHandler loader);
	
	
	public <D, T> void persistenceTypeHandlerRegistry_beforeRegisterTypeHandler(PersistenceTypeHandler<D, T> typeHandler);
	
	public <D, T> void persistenceTypeHandlerRegistry_beforeRegisterTypeHandlers(Iterable<? extends PersistenceTypeHandler<D, T>> typeHandlers);

	public <D, T> void persistenceTypeHandlerRegistry_afterRegisterTypeHandlers(long handlerCount);
	
	
	public void persistenceTypeNameMapper_afterMapClassName(String oldClassName, String mappedClassName);

	public void persistenceTypeNameMapper_afterMapInterfaceName(String oldInterfaceName, String mappedInterfaceName);
	
	
	public <D,T> void persistenceLegacyTypeMapper_beforeEnsureLegacyTypeHandler(PersistenceTypeDefinition legacyTypeDefinition, PersistenceTypeHandler<D, T> currentTypeHandler);

	public <D,T >void persistenceLegacyTypeMapper_afterEnsureLegacyTypeHandler(final PersistenceTypeDefinition legacyTypeDefinition, PersistenceLegacyTypeHandler<D, T> legacyTypeHandler);
			

	public <D> void persistenceLoaderCreator_beforeCreateLoader(
		PersistenceTypeHandlerLookup<D> typeLookup,
		PersistenceObjectRegistry registry,
		Persister persister,
		PersistenceSourceSupplier<D> source);

	public void persistenceLoaderCreator_afterCreateLoader(PersistenceLoaderLogging loader);
	
	
	public static PersistenceLogger set(final PersistenceLogger globalPersistenceLogger)
	{
		return Static.set(globalPersistenceLogger);
	}

	public static PersistenceLogger get()
	{
		return Static.get();
	}


	public static class Static
	{
		private static PersistenceLogger globalPersistenceLogger = load();

		static synchronized PersistenceLogger set(final PersistenceLogger globalPersistenceLogger)
		{
			final PersistenceLogger old = Static.globalPersistenceLogger;
			Static.globalPersistenceLogger = globalPersistenceLogger;
			return old;
		}

		static synchronized PersistenceLogger get()
		{
			if(globalPersistenceLogger == null)
			{
				globalPersistenceLogger = PersistenceLogger.New();
			}
			return globalPersistenceLogger;
		}

		private static PersistenceLogger load()
		{
			final Iterator<PersistenceLoggerProvider> iterator = ServiceLoader
				.load(PersistenceLoggerProvider.class)
				.iterator();
			return iterator.hasNext()
				? iterator.next().providePersistenceLogger()
				: null
			;
		}

		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		/**
		* Dummy constructor to prevent instantiation of this static-only utility class.
		*
		* @throws UnsupportedOperationException
		*/
		private Static()
		{
			// static only
			throw new UnsupportedOperationException();
		}

	}


	public static PersistenceLogger New()
	{
		return new Default();
	}


	public static PersistenceLogger NoOp()
	{
		return new NoOp();
	}


	public static abstract class Abstract implements PersistenceLogger
	{
		protected Abstract()
		{
			super();
		}
		
		@Override
		public void persistenceStorer_beforeStore(final PersistenceStorer storer, final Object instance)
		{
			// no-op
		}

		@Override
		public void persistenceStorer_afterStore(final PersistenceStorer storer, final Object instance, final long objectId)
		{
			// no-op
		}

		@Override
		public void persistenceStorer_beforeCommit(final PersistenceStorer storer)
		{
			// no-op
		}

		@Override
		public void persistenceStorer_afterCommit(final PersistenceStorer storer, final Object status)
		{
			// no-op
		}
		
		@Override
		public void persistenceStorer_afterRegisterLazy(final long objectId, final Object instance)
		{
			// no-op
		}
		
		@Override
		public void persistenceStorer_afterRegisterEager(final long objectId, final Object instance)
		{
			// no-op
		}

		@Override
		public void persistenceLoader_beforeLoadRoots()
		{
			// no-op
		}

		@Override
		public void persistenceLoader_afterLoadRoots(final PersistenceRoots loadedRoots)
		{
			// no-op
		}

		@Override
		public void persistenceLoader_beforeRegisterSkip(final long objectId)
		{
			// no-op
		}

		@Override
		public void persistenceLoader_afterRegisterSkip(final long objectId)
		{
			// no-op
		}

		@Override
		public void persistenceLoader_beforeGet()
		{
			// no-op
		}

		@Override
		public void persistenceLoader_afterGet(final Object retrieved)
		{
			// no-op
		}

		@Override
		public void persistenceLoader_beforeGetObject(final long objectId)
		{
			// no-op
		}

		@Override
		public void persistenceLoader_afterGetObject(final long objectId, final Object retrieved)
		{
			// no-op
		}

		@Override
		public <C> void persistenceLoader_beforeCollect(final C collector, final long[] objectIds)
		{
			// no-op
		}

		@Override
		public <C> void persistenceLoader_afterCollect(final C collected)
		{
			// no-op
		}
		
		@Override
		public <D, T> void persistenceTypeHandler_beforeStore(
			final PersistenceTypeHandler<D, T> typeHandler,
			final D data,
			final T instance,
			final long objectId,
			final PersistenceStoreHandler<D> storer)
		{
			// no-op
		}

		@Override
		public <D, T> void persistenceTypeHandler_afterStore(
			final PersistenceTypeHandler<D, T> typeHandler,
			final D data,
			final T instance,
			final long objectId,
			final PersistenceStoreHandler<D> storer)
		{
			// no-op
		}

		@Override
		public <D, T> void persistenceTypeHandler_beforeComplete(
			final PersistenceTypeHandler<D, T> typeHandler,
			final D data,
			final T instance,
			final PersistenceLoadHandler loader)
		{
			// no-op
		}

		@Override
		public <D, T> void persistenceTypeHandler_afterComplete(
			final PersistenceTypeHandler<D, T> typeHandler,
			final D data,
			final T instance,
			final PersistenceLoadHandler loader)
		{
			// no-op
		}
		
		@Override
		public <D, T> void persistenceTypeHandlerRegistry_beforeRegisterTypeHandler(final PersistenceTypeHandler<D, T> typeHandler)
		{
			//no-op
		}
		
		@Override
		public <D, T> void persistenceTypeHandlerRegistry_beforeRegisterTypeHandlers(
			final Iterable<? extends PersistenceTypeHandler<D, T>> typeHandlers)
		{
			//no-op
		}
		
		@Override
		public <D, T> void persistenceTypeHandlerRegistry_afterRegisterTypeHandlers(
			final long handlerCount)
		{
			//no-op
		}
		
		@Override
		public void persistenceTypeNameMapper_afterMapClassName(final String oldClassName, final String mappedClassName)
		{
			//no-op
		}

		@Override
		public void persistenceTypeNameMapper_afterMapInterfaceName(final String oldInterfaceName, final String mappedInterfaceName)
		{
			//no-op
		}
		
		@Override
		public <D,T> void persistenceLegacyTypeMapper_beforeEnsureLegacyTypeHandler(
			final PersistenceTypeDefinition legacyTypeDefinition, final PersistenceTypeHandler<D, T> currentTypeHandler)
		{
			//no-op
		}

		@Override
		public <D,T >void persistenceLegacyTypeMapper_afterEnsureLegacyTypeHandler(
			final PersistenceTypeDefinition legacyTypeDefinition, final PersistenceLegacyTypeHandler<D, T> legacyTypeHandler)
		{
			//no-op
		}
		
		@Override
		public <D> void persistenceLoaderCreator_beforeCreateLoader(
			final PersistenceTypeHandlerLookup<D> typeLookup,
			final PersistenceObjectRegistry registry,
			final Persister persister,
			final PersistenceSourceSupplier<D> source)
		{
			//no-op
		}

		@Override
		public void persistenceLoaderCreator_afterCreateLoader(final PersistenceLoaderLogging loader)
		{
			//no-op
		}
	}
	
	public static class NoOp extends Abstract
	{
		NoOp()
		{
			super();
		}
	}
	
	public static class Default extends Abstract
	{
		private final Logger loggerPersistenceStorer                   = LoggerFactory.get().forClass(PersistenceStorer.class);
		private final Logger loggerPersistenceLoader                   = LoggerFactory.get().forClass(PersistenceLoader.class);
		private final Logger loggerPersistenceTypeHandler              = LoggerFactory.get().forClass(PersistenceTypeHandler.class);
		private final Logger loggerPersistenceTypeHandlerRegistry      = LoggerFactory.get().forClass(PersistenceTypeHandlerRegistry.class);
		private final Logger loggerPersistenceTypeNameMapper           = LoggerFactory.get().forClass(PersistenceTypeNameMapper.class);
		private final Logger loggerPersistenceLegacyTypeMapper         = LoggerFactory.get().forClass(PersistenceLegacyTypeMapper.class);
		
		private final String logTag                                    = "microstream_persistence";

		protected Default()
		{
			super();
		}
		
		public Logger loggerPersistenceStorer()
		{
			return this.loggerPersistenceStorer;
		}

		public Logger loggerPersistenceTypeHandler()
		{
			return this.loggerPersistenceTypeHandler;
		}
		
		public Logger loggerPersistenceTypeHandlerRegistry()
		{
			return this.loggerPersistenceTypeHandlerRegistry;
		}

		public Logger loggerPersistenceTypeNameMapper()
		{
			return this.loggerPersistenceTypeNameMapper;
		}
		
		@Override
		public void persistenceStorer_afterStore(final PersistenceStorer storer, final Object instance, final long objectId)
		{
			this.loggerPersistenceStorer.debug()
				.withTag(this.logTag)
				.log("Stored instance: %s, objectId=%d", instance, objectId)
			;
		}

		@Override
		public void persistenceStorer_afterCommit(final PersistenceStorer storer, final Object status)
		{
			this.loggerPersistenceStorer.debug()
				.withTag(this.logTag)
				.log("Commited %d instances", storer.size())
			;
		}
		
		@Override
		public void persistenceStorer_afterRegisterLazy(final long objectId, final Object instance)
		{
			this.loggerPersistenceStorer.debug()
				.withTag(this.logTag)
				.log("Object registered [lazy], objectId=%s: %s", objectId, instance)
			;
		}
		
		@Override
		public void persistenceStorer_afterRegisterEager(final long objectId, final Object instance)
		{
			this.loggerPersistenceStorer.debug()
				.withTag(this.logTag)
				.log("Object registered [eager], objectId=%s: %s", objectId, instance)
			;
		}

		@Override
		public void persistenceLoader_beforeLoadRoots()
		{
			this.loggerPersistenceLoader.debug()
				.withTag(this.logTag)
				.log("Loading persistence roots")
			;
		}
		
		@Override
		public void persistenceLoader_afterLoadRoots(final PersistenceRoots loadedRoots)
		{
			this.loggerPersistenceLoader.debug()
				.withTag(this.logTag)
				.log("Loaded persistence roots %s", loadedRoots)
			;
			
			if(loadedRoots == null)
			{
				this.loggerPersistenceLoader.debug()
					.withTag(this.logTag)
					.log("No persistence roots loaded");
			}
			else
			{
				for (final KeyValue<String, Object> iterable_element : loadedRoots.entries())
				{
					this.loggerPersistenceLoader.debug()
						.withTag(this.logTag)
						.log("loaded root element %s %s", iterable_element.key(), iterable_element.value())
					;
				}
			}
		}
		
		@Override
		public void persistenceLoader_beforeGetObject(final long objectId)
		{
			this.loggerPersistenceLoader.debug()
				.withTag(this.logTag)
				.log("Loading object by ID %d", objectId)
			;
		}
		
		@Override
		public void persistenceLoader_afterGetObject(final long objectId, final Object retrieved)
		{
			this.loggerPersistenceLoader.debug()
				.withTag(this.logTag)
				.log("Loaded object %s by ID %d", retrieved, objectId)
			;
		}
		
		@Override
		public <D, T> void persistenceTypeHandler_beforeStore(
			final PersistenceTypeHandler<D, T> typeHandler,
			final D data,
			final T instance,
			final long objectId,
			final PersistenceStoreHandler<D> storer)
		{
			if(instance instanceof PersistenceRoots)
			{
				this.loggerPersistenceTypeHandler.debug()
					.withTag(this.logTag)
					.log("Storing persistence roots")
				;
			}
		}
		
		@Override
		public <D, T> void persistenceTypeHandler_afterStore(
			final PersistenceTypeHandler<D, T> typeHandler,
			final D data,
			final T instance,
			final long objectId,
			final PersistenceStoreHandler<D> storer)
		{
			this.loggerPersistenceTypeHandler.debug()
				.withTag(this.logTag)
				.log("Stored instance %s, oid=%d", typeHandler.typeName(), objectId)
			;
		}
		
		@Override
		public <D, T> void persistenceTypeHandler_afterComplete(
			final PersistenceTypeHandler<D, T> typeHandler,
			final D data,
			final T instance,
			final PersistenceLoadHandler loader)
		{
			this.loggerPersistenceTypeHandler.debug()
				.withTag(this.logTag)
				.log("Loaded instance %s", typeHandler.typeName())
			;
		}
		
		@Override
		public void persistenceTypeNameMapper_afterMapClassName(final String oldClassName, final String mappedClassName)
		{
			this.loggerPersistenceTypeNameMapper.debug()
				.withTag(this.logTag)
				.log("mapped class %s to %s", oldClassName, mappedClassName)
			;
		}

		@Override
		public void persistenceTypeNameMapper_afterMapInterfaceName(final String oldInterfaceName, final String mappedInterfaceName)
		{
			this.loggerPersistenceTypeNameMapper.debug()
				.withTag(this.logTag)
				.log("mapped inferface %s", oldInterfaceName, mappedInterfaceName)
			;
		}
		
		@Override
		public <D, T> void persistenceTypeHandlerRegistry_beforeRegisterTypeHandler(final PersistenceTypeHandler<D, T> typeHandler)
		{
			this.loggerPersistenceTypeHandlerRegistry.debug()
				.withTag(this.logTag)
				.log("registering type handler: %s" ,
					typeHandler.getClass() +
					" for type: " + typeHandler.typeName() +
					" with type_id: " + typeHandler.typeId())
			;
		}
		
		@Override
		public <D, T> void persistenceTypeHandlerRegistry_beforeRegisterTypeHandlers(
			final Iterable<? extends PersistenceTypeHandler<D, T>> typeHandlers)
		{
										
			this.loggerPersistenceTypeHandlerRegistry.debug()
				.withTag(this.logTag)
				.log("registering type handlers: \n%s",
					StreamSupport.stream(typeHandlers.spliterator(), false)
						.map(th -> th.getClass().toString() +
							" for type: " + th.typeName() +
							" with type_id: " + th.typeId())
						.collect(Collectors.joining("\n") ))
			;
		}
		
		@Override
		public <D, T> void persistenceTypeHandlerRegistry_afterRegisterTypeHandlers(final long handlerCount)
		{
			this.loggerPersistenceTypeHandlerRegistry.debug()
				.withTag(this.logTag)
				.log("registered %d TypeHandlers" , handlerCount)
			;
		}
		
		@Override
		public <D,T> void persistenceLegacyTypeMapper_beforeEnsureLegacyTypeHandler(
			final PersistenceTypeDefinition legacyTypeDefinition, final PersistenceTypeHandler<D, T> currentTypeHandler)
		{
			this.loggerPersistenceLegacyTypeMapper.debug()
				.withTag(this.logTag)
				.log("ensure legacy type handler for type: %s current handler: %s", legacyTypeDefinition, currentTypeHandler)
			;
		}

		@Override
		public <D,T >void persistenceLegacyTypeMapper_afterEnsureLegacyTypeHandler(
			final PersistenceTypeDefinition legacyTypeDefinition, final PersistenceLegacyTypeHandler<D, T> legacyTypeHandler)
		{
			this.loggerPersistenceLegacyTypeMapper.debug()
				.withTag(this.logTag)
				.log("ensured legacy type handler for type: %s legacyTypeHandler: %s", legacyTypeDefinition, legacyTypeHandler)
			;
		}
		
		@Override
		public <D> void persistenceLoaderCreator_beforeCreateLoader(
			final PersistenceTypeHandlerLookup<D> typeLookup,
			final PersistenceObjectRegistry	registry,
			final Persister persister,
			final PersistenceSourceSupplier<D> source)
		{
			this.loggerPersistenceLoader.debug()
				.withTag(this.logTag)
				.log("Creating persistence loader");
		}

		@Override
		public void persistenceLoaderCreator_afterCreateLoader(final PersistenceLoaderLogging loader)
		{
			this.loggerPersistenceLoader.debug()
				.withTag(this.logTag)
				.log("Created persistence loader %s", loader);
		}
	}






}
