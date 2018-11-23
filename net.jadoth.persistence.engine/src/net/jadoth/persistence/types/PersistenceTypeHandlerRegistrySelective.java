package net.jadoth.persistence.types;

import static net.jadoth.X.notNull;

import java.util.function.Consumer;

import net.jadoth.collections.HashMapIdObject;
import net.jadoth.collections.MiniMap;
import net.jadoth.persistence.exceptions.PersistenceExceptionConsistency;
import net.jadoth.persistence.exceptions.PersistenceExceptionConsistencyUnknownObject;
import net.jadoth.persistence.exceptions.PersistenceExceptionConsistencyWrongTypeId;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeHandlerConsistencyWrongHandler;

public interface PersistenceTypeHandlerRegistrySelective<M> extends PersistenceTypeHandlerRegistry<M>
{
	public <T> boolean register(T object, PersistenceTypeHandler<M, T> typeHandler);



	public class Implementation<M> implements PersistenceTypeHandlerRegistrySelective<M>
	{
		/* (22.08.2012)XXX: Overhaul PersistenceTypeHandlerRegistrySelective prototype
		 * 1.) iterate local TypeHandlers as well (?)
		 * 2.) maybe optimize dual maps
		 * 3.) what about weak referencing the objects?
		 * 4.) add unregister, bulkregister, etc. method or allow public access to DualHashMapObject_long implementation
		 * 5.) make properly and efficiently thread safe
		 */

		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final PersistenceTypeHandlerRegistry<M> handlerRegistry;
		private final PersistenceObjectLookup           objectLookup   ;

		private final HashMapIdObject<PersistenceTypeHandler<M, ?>> oidToHandler = HashMapIdObject.New();
		private final MiniMap<Object, PersistenceTypeHandler<M, ?>> objToHandler = new MiniMap<>();



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(
			final PersistenceTypeHandlerRegistry<M> handlerRegistry,
			final PersistenceObjectLookup objectLookup
		)
		{
			super();
			this.handlerRegistry = notNull(handlerRegistry);
			this.objectLookup    = notNull(objectLookup   );
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public long lookupTypeId(final Class<?> type)
		{
			return this.handlerRegistry.lookupTypeId(type);
		}

		@Override
		public <T> Class<T> lookupType(final long typeId)
		{
			return this.handlerRegistry.lookupType(typeId);
		}
		
		@Override
		public boolean validateTypeMapping(final long typeId, final Class<?> type) throws PersistenceExceptionConsistency
		{
			return this.handlerRegistry.validateTypeMapping(typeId, type);
		}
		
		@Override
		public boolean validateTypeMappings(final Iterable<? extends PersistenceTypeLink> mappings)
			throws PersistenceExceptionConsistency
		{
			return this.handlerRegistry.validateTypeMappings(mappings);
		}

		@Override
		public boolean registerType(final long tid, final Class<?> type) throws PersistenceExceptionConsistency
		{
			return this.handlerRegistry.registerType(tid, type);
		}
		
		@Override
		public boolean registerTypes(final Iterable<? extends PersistenceTypeLink> types)
			throws PersistenceExceptionConsistency
		{
			return this.handlerRegistry.registerTypes(types);
		}

		@Override
		public boolean registerTypeHandler(final PersistenceTypeHandler<M, ?> type)
		{
			return this.handlerRegistry.registerTypeHandler(type);
		}
		
		@Override
		public final boolean registerLegacyTypeHandler(final PersistenceLegacyTypeHandler<M, ?> legacyTypeHandler)
		{
			return this.handlerRegistry.registerLegacyTypeHandler(legacyTypeHandler);
		}

		@Override
		public <T> PersistenceTypeHandler<M, T> lookupTypeHandler(final Class<T> type)
		{
			return this.handlerRegistry.lookupTypeHandler(type);
		}

		@Override
		public PersistenceTypeHandler<M, ?> lookupTypeHandler(final long typeId)
		{
			return this.handlerRegistry.lookupTypeHandler(typeId);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> PersistenceTypeHandler<M, T> lookupTypeHandler(final T instance)
		{
			PersistenceTypeHandler<M, ?> handler;
			if((handler = this.objToHandler.get(instance)) == null)
			{
				handler = this.handlerRegistry.lookupTypeHandler(instance);
			}
			return (PersistenceTypeHandler<M, T>)handler;
		}

		@Override
		public PersistenceTypeHandler<M, ?> lookupTypeHandler(final long objectId, final long typeId)
		{
			PersistenceTypeHandler<M, ?> handler;
			if((handler = this.oidToHandler.get(objectId)) == null)
			{
				handler = this.handlerRegistry.lookupTypeHandler(objectId, typeId);
			}
			return handler;
		}

		@Override
		public synchronized <T> boolean register(final T object, final PersistenceTypeHandler<M, T> typeHandler)
		{
			if(object.getClass() != typeHandler.type())
			{
				throw new PersistenceExceptionTypeHandlerConsistencyWrongHandler(object.getClass(), typeHandler);
			}
			final long oid;
			if((oid = this.objectLookup.lookupObjectId(object)) == 0L)
			{
				throw new PersistenceExceptionConsistencyUnknownObject(object);
			}
			final long tid;
			if((tid = this.handlerRegistry.lookupTypeId(object.getClass())) != typeHandler.typeId())
			{
				throw new PersistenceExceptionConsistencyWrongTypeId(object.getClass(), tid, typeHandler.typeId());
			}

			this.oidToHandler.put(oid, typeHandler);
			return this.objToHandler.put(object, typeHandler) != typeHandler;
		}
		
		@Override
		public <C extends Consumer<? super PersistenceTypeHandler<M, ?>>> C iterateTypeHandlers(final C iterator)
		{
			this.handlerRegistry.iterateTypeHandlers(iterator);
			return iterator;
		}

	}

}
