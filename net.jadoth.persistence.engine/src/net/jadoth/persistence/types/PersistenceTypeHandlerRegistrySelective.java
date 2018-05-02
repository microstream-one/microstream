package net.jadoth.persistence.types;

import static net.jadoth.Jadoth.notNull;

import java.util.function.Consumer;

import net.jadoth.collections.HashMapIdObject;
import net.jadoth.collections.MiniMap;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeHandlerConsistencyWrongHandler;
import net.jadoth.swizzling.exceptions.SwizzleExceptionConsistency;
import net.jadoth.swizzling.exceptions.SwizzleExceptionConsistencyUnknownObject;
import net.jadoth.swizzling.exceptions.SwizzleExceptionConsistencyWrongTypeId;
import net.jadoth.swizzling.types.SwizzleObjectLookup;
import net.jadoth.swizzling.types.SwizzleTypeLink;

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
		private final SwizzleObjectLookup                    objectLookup   ;

		private final HashMapIdObject<PersistenceTypeHandler<M, ?>> oidToHandler = HashMapIdObject.New();
		private final MiniMap<Object, PersistenceTypeHandler<M, ?>> objToHandler = new MiniMap<>();



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(
			final PersistenceTypeHandlerRegistry<M> handlerRegistry,
			final SwizzleObjectLookup objectLookup
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
		public void validateTypeMapping(final long typeId, final Class<?> type)
		{
			this.handlerRegistry.validateTypeMapping(typeId, type);
		}

		@Override
		public boolean register(final PersistenceTypeHandler<M, ?> type)
		{
			return this.handlerRegistry.register(type);
		}

		@Override
		public <T> PersistenceTypeHandler<M, T> lookupTypeHandler(final Class<T> type)
		{
			return this.handlerRegistry.lookupTypeHandler(type);
		}

		@Override
		public <T> PersistenceTypeHandler<M, T> lookupTypeHandler(final long typeId)
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

		@SuppressWarnings("unchecked")
		@Override
		public <T> PersistenceTypeHandler<M, T> lookupTypeHandler(final long objectId, final long typeId)
		{
			PersistenceTypeHandler<M, ?> handler;
			if((handler = this.oidToHandler.get(objectId)) == null)
			{
				handler = this.handlerRegistry.lookupTypeHandler(objectId, typeId);
			}
			return (PersistenceTypeHandler<M, T>)handler;
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
				throw new SwizzleExceptionConsistencyUnknownObject(object);
			}
			final long tid;
			if((tid = this.handlerRegistry.lookupTypeId(object.getClass())) != typeHandler.typeId())
			{
				throw new SwizzleExceptionConsistencyWrongTypeId(object.getClass(), tid, typeHandler.typeId());
			}

			this.oidToHandler.put(oid, typeHandler);
			return this.objToHandler.put(object, typeHandler) != typeHandler;
		}

		@Override
		public boolean registerType(final long tid, final Class<?> type) throws SwizzleExceptionConsistency
		{
			return this.handlerRegistry.registerType(tid, type);
		}

		@Override
		public void validateExistingTypeMappings(final XGettingSequence<? extends SwizzleTypeLink<?>> mappings)
			throws SwizzleExceptionConsistency
		{
			this.handlerRegistry.validateExistingTypeMappings(mappings);
		}

		@Override
		public void validatePossibleTypeMappings(final XGettingSequence<? extends SwizzleTypeLink<?>> mappings)
			throws SwizzleExceptionConsistency
		{
			this.handlerRegistry.validatePossibleTypeMappings(mappings);
		}

		@Override
		public void iterateTypeHandlers(final Consumer<? super PersistenceTypeHandler<M, ?>> procedure)
		{
			this.handlerRegistry.iterateTypeHandlers(procedure);
		}

	}

}
