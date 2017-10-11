package net.jadoth.persistence.types;

import java.util.function.Consumer;

import net.jadoth.Jadoth;
import net.jadoth.collections.HashMapIdObject;
import net.jadoth.collections.MiniMap;
import net.jadoth.collections.types.XGettingMap;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeHandlerConsistencyConflictedType;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeHandlerConsistencyConflictedTypeId;
import net.jadoth.swizzling.exceptions.SwizzleExceptionConsistency;
import net.jadoth.swizzling.types.SwizzleTypeLink;
import net.jadoth.swizzling.types.SwizzleTypeRegistry;

public interface PersistenceTypeHandlerRegistry<M>
extends PersistenceTypeHandlerLookup<M>, SwizzleTypeRegistry, PersistenceTypeHandlerIterable<M>
{
	public boolean register(PersistenceTypeHandler<M, ?> type);



	public final class Implementation<M> implements PersistenceTypeHandlerRegistry<M>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final SwizzleTypeRegistry typeRegistry;

		private final MiniMap<Class<?>, PersistenceTypeHandler<M, ?>> t2h = new MiniMap<>();
		private final HashMapIdObject<PersistenceTypeHandler<M, ?>>   i2h = HashMapIdObject.New();



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(final SwizzleTypeRegistry typeRegistry)
		{
			super();
			this.typeRegistry = typeRegistry;
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

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
		
		@Override
		public final long typeCount()
		{
			return this.typeRegistry.typeCount();
		}

		@SuppressWarnings("unchecked") // cast type safety guaranteed by management logic
		@Override
		public <T> PersistenceTypeHandler<M, T> lookupTypeHandler(final Class<T> type)
		{
			return (PersistenceTypeHandler<M, T>)this.t2h.get(type);
		}

		@SuppressWarnings("unchecked") // cast type safety guaranteed by management logic
		@Override
		public <T> PersistenceTypeHandler<M, T> lookupTypeHandler(final long typeId)
		{
			return (PersistenceTypeHandler<M, T>)this.i2h.get(typeId);
		}

		@Override
		public <T> PersistenceTypeHandler<M, T> lookupTypeHandler(final T instance)
		{
			// standard registry does not consider actual objects
			return this.lookupTypeHandler(Jadoth.getClass(instance));
		}

		@Override
		public <T> PersistenceTypeHandler<M, T> lookupTypeHandler(final long objectId, final long typeId)
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
		public void validatePossibleTypeMapping(final long typeId, final Class<?> type)
		{
			this.typeRegistry.validatePossibleTypeMapping(typeId, type);
		}

		@Override
		public boolean register(final PersistenceTypeHandler<M, ?> typeHandler)
		{
			synchronized(this.t2h)
			{
				final Class<?> type = typeHandler.type();
				final long     tid  = typeHandler.typeId();
				this.typeRegistry.registerType(tid, type); // first ensure consistency of tid<->type combination

				// check if handler is already registered for type
				PersistenceTypeHandler<M, ?> actualHandler;
				if((actualHandler = this.t2h.get(type)) != null)
				{
					if(actualHandler != typeHandler)
					{
						throw new PersistenceExceptionTypeHandlerConsistencyConflictedType(type, actualHandler, typeHandler);
					}
					// else: fall through to tid check
				}
				// else: handler is not registered yet, proceed with tid check

				// check if handler is already registered for tid
				if((actualHandler = this.i2h.get(tid)) != null)
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
			this.t2h.put(typeHandler.type(), typeHandler);
			this.i2h.put(typeHandler.typeId(), typeHandler);
		}

		public void clear()
		{
			synchronized(this.t2h)
			{
				this.t2h.clear();
				this.i2h.clear();
			}
		}
		
		@Override
		public <C extends Consumer<? super PersistenceTypeHandler<M, ?>>> C iterateTypeHandlers(final C iterator)
		{
			this.t2h.iterateValues(iterator);
			return iterator;
		}

		@Override
		public boolean registerType(final long tid, final Class<?> type) throws SwizzleExceptionConsistency
		{
			return this.typeRegistry.registerType(tid, type);
		}
		
		@Override
		public long ensureRegisteredType(final Class<?> type, final long tid) throws SwizzleExceptionConsistency
		{
			return this.typeRegistry.ensureRegisteredType(type, tid);
		}
		
		@Override
		public long ensureRegisteredTypes(final XGettingMap<Class<?>, Long> typeMapping)
		{
			return this.typeRegistry.ensureRegisteredTypes(typeMapping);
		}

		@Override
		public void validateExistingTypeMappings(final XGettingSequence<? extends SwizzleTypeLink<?>> mappings)
			throws SwizzleExceptionConsistency
		{
			this.typeRegistry.validateExistingTypeMappings(mappings);
		}

		@Override
		public void validatePossibleTypeMappings(final XGettingSequence<? extends SwizzleTypeLink<?>> mappings)
			throws SwizzleExceptionConsistency
		{
			this.typeRegistry.validatePossibleTypeMappings(mappings);
		}

	}

}
