package one.microstream.persistence.types;

/*-
 * #%L
 * microstream-persistence
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

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import one.microstream.collections.HashMapIdObject;
import one.microstream.collections.MiniMap;
import one.microstream.persistence.exceptions.PersistenceExceptionConsistency;
import one.microstream.persistence.exceptions.PersistenceExceptionTypeHandlerConsistencyConflictedType;
import one.microstream.persistence.exceptions.PersistenceExceptionTypeHandlerConsistencyConflictedTypeId;
import one.microstream.reflect.XReflect;

public interface PersistenceTypeHandlerRegistry<D>
extends PersistenceTypeHandlerLookup<D>, PersistenceTypeRegistry, PersistenceTypeHandlerIterable<D>
{
	public <T> boolean registerTypeHandler(PersistenceTypeHandler<D, T> typeHandler);
	
	public <T> long registerTypeHandlers(Iterable<? extends PersistenceTypeHandler<D, T>> typeHandlers);
	
	public <T> boolean registerTypeHandler(Class<T> type, PersistenceTypeHandler<D, ? super T> typeHandler);
	
	public boolean registerLegacyTypeHandler(PersistenceLegacyTypeHandler<D, ?> legacyTypeHandler);
	
	
	

	public static <D> PersistenceTypeHandlerRegistry.Default<D> New(
		final PersistenceTypeRegistry typeRegistry
	)
	{
		return new PersistenceTypeHandlerRegistry.Default<>(
			notNull(typeRegistry)
		);
	}

	public final class Default<D> implements PersistenceTypeHandlerRegistry<D>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final PersistenceTypeRegistry typeRegistry;

		private final MiniMap<Class<?>, PersistenceTypeHandler<D, ?>> handlersByType   = new MiniMap<>();
		private final HashMapIdObject<PersistenceTypeHandler<D, ?>>   handlersByTypeId = HashMapIdObject.New();



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final PersistenceTypeRegistry typeRegistry)
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
		public <T> PersistenceTypeHandler<D, T> lookupTypeHandler(final Class<T> type)
		{
			return (PersistenceTypeHandler<D, T>)this.handlersByType.get(type);
		}

		@Override
		public PersistenceTypeHandler<D, ?> lookupTypeHandler(final long typeId)
		{
			return this.handlersByTypeId.get(typeId);
		}

		@Override
		public <T> PersistenceTypeHandler<D, T> lookupTypeHandler(final T instance)
		{
			// standard registry does not consider actual objects
			return this.lookupTypeHandler(XReflect.getClass(instance));
		}
		
		@Override
		public boolean validateTypeMapping(final long typeId, final Class<?> type) throws PersistenceExceptionConsistency
		{
			return this.typeRegistry.validateTypeMapping(typeId, type);
		}
		
		@Override
		public boolean validateTypeMappings(final Iterable<? extends PersistenceTypeLink> mappings)
			throws PersistenceExceptionConsistency
		{
			return this.typeRegistry.validateTypeMappings(mappings);
		}

		@Override
		public boolean registerType(final long tid, final Class<?> type) throws PersistenceExceptionConsistency
		{
			return this.typeRegistry.registerType(tid, type);
		}
		
		@Override
		public boolean registerTypes(final Iterable<? extends PersistenceTypeLink> types)
			throws PersistenceExceptionConsistency
		{
			return this.typeRegistry.registerTypes(types);
		}
		

		@Override
		public <T> boolean registerTypeHandler(
			final Class<T>                             type       ,
			final PersistenceTypeHandler<D, ? super T> typeHandler
		)
		{
			synchronized(this.handlersByType)
			{
				if(this.synchValidateAlreadyRegisteredTypeHandler(type, typeHandler))
				{
					return true;
				}
				
				this.registerTypeHandler(typeHandler);
				
				this.synchPutTypeMapping(type, typeHandler);
				
				// when does this method ever return false? Not registerable case is handled via exception
				return true;
			}
		}
		
		private <T> boolean synchValidateAlreadyRegisteredTypeHandler(
			final Class<T>                             type       ,
			final PersistenceTypeHandler<D, ? super T> typeHandler
		)
		{
			PersistenceTypeHandler<D, ?> actualHandler;
			if((actualHandler = this.handlersByType.get(type)) == null)
			{
				return false;
			}
			
			if(actualHandler == typeHandler)
			{
				return true;
			}

			throw new PersistenceExceptionTypeHandlerConsistencyConflictedType(type, actualHandler, typeHandler);
		}

		@Override
		public <T> boolean registerTypeHandler(final PersistenceTypeHandler<D, T> typeHandler)
		{
			synchronized(this.handlersByType)
			{
				final Class<T> type = typeHandler.type();
				final long     tid  = typeHandler.typeId();
				this.typeRegistry.registerType(tid, type); // first ensure consistency of tid<->type combination

				// check if handler is already registered for type
				this.synchValidateAlreadyRegisteredTypeHandler(type, typeHandler);

				// else: handler is not registered yet, proceed with tid check

				// check if a handler is already registered for the same tid
				if(this.synchCheckByTypeId(typeHandler))
				{
					// redundant registering attempt, abort.
					return false;
				}
				// else: handler, tid, type combination is neither registered nor inconsistent, so register handler.

				// register new bidirectional assignment
				// note: basic type<->tid registration already happened above if necessary
				this.synchPutFullMapping(typeHandler);
				
				return true;
			}
		}

		@Override
		public <T> long registerTypeHandlers(
			final Iterable<? extends PersistenceTypeHandler<D, T>> typeHandlers
		)
		{
			synchronized(this.handlersByType)
			{
				long registeredCount = 0;
				for(final PersistenceTypeHandler<D, T> handler : typeHandlers)
				{
					if(this.registerTypeHandler(handler))
					{
						registeredCount++;
					}
				}
				
				return registeredCount;
			}
		}

		private <T> void synchPutTypeMapping(
			final Class<T>                             type       ,
			final PersistenceTypeHandler<D, ? super T> typeHandler
		)
		{
			this.handlersByType.put(type, typeHandler);
		}

		private <T> void synchPutFullMapping(final PersistenceTypeHandler<D, T> typeHandler)
		{
			this.synchPutTypeMapping(typeHandler.type(), typeHandler);
			this.handlersByTypeId.put(typeHandler.typeId(), typeHandler);
		}
		
		private boolean synchCheckByTypeId(final PersistenceTypeHandler<D, ?> typeHandler)
		{
			final PersistenceTypeHandler<D, ?> actualHandler;
			if((actualHandler = this.handlersByTypeId.get(typeHandler.typeId())) != null)
			{
				if(actualHandler != typeHandler)
				{
					throw new PersistenceExceptionTypeHandlerConsistencyConflictedTypeId(
						typeHandler.typeId(),
						actualHandler,
						typeHandler
					);
				}
				// else: handler is already consistently registered.
				return true;
			}
			
			return false;
		}
		

		@Override
		public boolean registerLegacyTypeHandler(final PersistenceLegacyTypeHandler<D, ?> legacyTypeHandler)
		{
			synchronized(this.handlersByType)
			{
				// check if a handler is already registered for the same tid
				if(this.synchCheckByTypeId(legacyTypeHandler))
				{
					// redundant registering attempt, abort.
					return false;
				}
				
				// no registration by type, just by typeId. This is a one-way translation helper for lookups by TID.
				this.handlersByTypeId.put(legacyTypeHandler.typeId(), legacyTypeHandler);
				
				return true;
			}
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
		public <C extends Consumer<? super PersistenceTypeHandler<D, ?>>> C iterateTypeHandlers(
			final C iterator
		)
		{
			synchronized(this.handlersByType)
			{
				this.handlersByType.iterateValues(iterator);
			}
			return iterator;
		}
		
		@Override
		public <C extends Consumer<? super PersistenceLegacyTypeHandler<D, ?>>> C iterateLegacyTypeHandlers(
			final C iterator
		)
		{
			synchronized(this.handlersByType)
			{
				this.handlersByTypeId.iterateValues(th ->
				{
					if(th instanceof PersistenceLegacyTypeHandler)
					{
						iterator.accept((PersistenceLegacyTypeHandler<D, ?>)th);
					}
				});
			}
			
			return iterator;
		}
		
		@Override
		public <C extends Consumer<? super PersistenceTypeHandler<D, ?>>> C iterateAllTypeHandlers(final C iterator)
		{
			synchronized(this.handlersByType)
			{
				this.handlersByTypeId.iterateValues(iterator);
			}
			
			return iterator;
		}

		@Override
		public void iteratePerIds(final BiConsumer<Long, ? super Class<?>> consumer)
		{
			this.typeRegistry.iteratePerIds(consumer);
		}
		
	}

}
