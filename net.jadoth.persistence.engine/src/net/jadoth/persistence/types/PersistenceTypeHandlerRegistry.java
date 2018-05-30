package net.jadoth.persistence.types;

import java.util.function.Consumer;

import net.jadoth.collections.HashMapIdObject;
import net.jadoth.collections.MiniMap;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeHandlerConsistencyConflictedType;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeHandlerConsistencyConflictedTypeId;
import net.jadoth.reflect.XReflect;
import net.jadoth.swizzling.exceptions.SwizzleExceptionConsistency;
import net.jadoth.swizzling.types.SwizzleTypeLink;
import net.jadoth.swizzling.types.SwizzleTypeRegistry;

public interface PersistenceTypeHandlerRegistry<M>
extends PersistenceTypeHandlerLookup<M>, SwizzleTypeRegistry, PersistenceTypeHandlerIterable<M>
{
	public boolean registerTypeHandler(PersistenceTypeHandler<M, ?> type);
	


	public final class Implementation<M> implements PersistenceTypeHandlerRegistry<M>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final SwizzleTypeRegistry typeRegistry;

		private final MiniMap<Class<?>, PersistenceTypeHandler<M, ?>> handlersByType   = new MiniMap<>();
		private final HashMapIdObject<PersistenceTypeHandler<M, ?>>   handlersByTypeId = HashMapIdObject.New();



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(final SwizzleTypeRegistry typeRegistry)
		{
			super();
			this.typeRegistry = typeRegistry;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public long lookupTypeId(final Class<?> type)
		{
			return this.typeRegistry.lookupTypeId(type);
		}

		@Override
		public <T> Class<T> lookupType(final long typeId)
		{
			return this.typeRegistry.lookupType(typeId);
		}

		@SuppressWarnings("unchecked") // cast type safety guaranteed by management logic
		@Override
		public <T> PersistenceTypeHandler<M, T> lookupTypeHandler(final Class<T> type)
		{
			return (PersistenceTypeHandler<M, T>)this.handlersByType.get(type);
		}

		@Override
		public PersistenceTypeHandler<M, ?> lookupTypeHandler(final long typeId)
		{
			return this.handlersByTypeId.get(typeId);
		}

		@Override
		public <T> PersistenceTypeHandler<M, T> lookupTypeHandler(final T instance)
		{
			// standard registry does not consider actual objects
			return this.lookupTypeHandler(XReflect.getClass(instance));
		}

		@Override
		public PersistenceTypeHandler<M, ?> lookupTypeHandler(final long objectId, final long typeId)
		{
			// standard registry does not consider actual objects
			return this.lookupTypeHandler(typeId);
		}

		@Override
		public void validateExistingTypeMapping(final long typeId, final Class<?> type)
		{
			this.typeRegistry.validateExistingTypeMapping(typeId, type);
		}

		@Override
		public boolean registerTypeHandler(final PersistenceTypeHandler<M, ?> typeHandler)
		{
			synchronized(this.handlersByType)
			{
				final Class<?> type = typeHandler.type();
				final long     tid  = typeHandler.typeId();
				this.typeRegistry.registerType(tid, type); // first ensure consistency of tid<->type combination

				// check if handler is already registered for type
				PersistenceTypeHandler<M, ?> actualHandler;
				if((actualHandler = this.handlersByType.get(type)) != null)
				{
					if(actualHandler != typeHandler)
					{
						throw new PersistenceExceptionTypeHandlerConsistencyConflictedType(type, actualHandler, typeHandler);
					}
					// else: fall through to tid check
				}
				// else: handler is not registered yet, proceed with tid check

				// check if handler is already registered for tid
				if((actualHandler = this.handlersByTypeId.get(tid)) != null)
				{
					if(actualHandler != typeHandler)
					{
						throw new PersistenceExceptionTypeHandlerConsistencyConflictedTypeId(tid, actualHandler, typeHandler);
					}
					// else: handler is already consistently registered. Hence redundant registering, abort.
					return false;
				}
				// else: handler, tid, type combination is neither registered nor inconsistent, so register handler.

				// register new bidirectional assignment
				// note: basic type<->tid registration already happened above if necessary
				this.putMapping(typeHandler);
				return true;
			}
		}

		private void putMapping(final PersistenceTypeHandler<M, ?> typeHandler)
		{
			this.handlersByType.put(typeHandler.type(), typeHandler);
			this.handlersByTypeId.put(typeHandler.typeId(), typeHandler);
		}

		public void clear()
		{
			synchronized(this.handlersByType)
			{
				this.handlersByType.clear();
				this.handlersByTypeId.clear();
			}
		}

		@Override
		public <C extends Consumer<? super PersistenceTypeHandler<M, ?>>> C iterateTypeHandlers(final C iterator)
		{
			this.handlersByType.iterateValues(iterator);
			return iterator;
		}

		@Override
		public boolean registerType(final long tid, final Class<?> type) throws SwizzleExceptionConsistency
		{
			return this.typeRegistry.registerType(tid, type);
		}

		@Override
		public void validateExistingTypeMappings(final Iterable<? extends SwizzleTypeLink<?>> mappings)
			throws SwizzleExceptionConsistency
		{
			this.typeRegistry.validateExistingTypeMappings(mappings);
		}

		@Override
		public void validatePossibleTypeMappings(final Iterable<? extends SwizzleTypeLink<?>> mappings)
			throws SwizzleExceptionConsistency
		{
			this.typeRegistry.validatePossibleTypeMappings(mappings);
		}

	}

}
