package one.microstream.persistence.types;

import java.util.function.Consumer;

import one.microstream.collections.HashEnum;
import one.microstream.collections.HashTable;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XGettingEnum;

public interface PersistenceCustomTypeHandlerRegistry<D> extends PersistenceTypeHandlerIterable<D>
{
	public <T> boolean registerTypeHandler(PersistenceTypeHandler<D, T> typeHandler);

	public <T> boolean registerTypeHandler(Class<T> type, PersistenceTypeHandler<D, ? super T> typeHandler);
	
	public <T> boolean registerLegacyTypeHandler(PersistenceLegacyTypeHandler<D, T> legacyTypeHandler);
	
	public PersistenceCustomTypeHandlerRegistry<D> registerLegacyTypeHandlers(
		XGettingCollection<? extends PersistenceLegacyTypeHandler<D, ?>> legacyTypeHandlers
	);

	public PersistenceCustomTypeHandlerRegistry<D> registerTypeHandlers(
		XGettingCollection<? extends PersistenceTypeHandler<D, ?>> typeHandlers
	);
	
	public <T> PersistenceTypeHandler<D, ? super T> lookupTypeHandler(Class<T> type);
		
	public XGettingEnum<PersistenceLegacyTypeHandler<D, ?>> legacyTypeHandlers();

	public boolean knowsType(Class<?> type);
		
	
	
	public static <D> PersistenceCustomTypeHandlerRegistry.Default<D> New()
	{
		return new PersistenceCustomTypeHandlerRegistry.Default<>();
	}

	public final class Default<D> implements PersistenceCustomTypeHandlerRegistry<D>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final HashTable<Class<?>, PersistenceTypeHandler<D, ?>> liveTypeHandlers = HashTable.New();
		
		/*
		 * Really instance equality since:
		 * - TypeId might not be present, yet.
		 * - Live type cannot be used for LTHs.
		 * - This is just a collection of "potentially structure-compatible" handlers that get sorted out later.
		 */
		private final HashEnum<PersistenceLegacyTypeHandler<D, ?>> legacyTypeHandlers = HashEnum.New();

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default()
		{
			super();
		}
		


		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public synchronized boolean knowsType(final Class<?> type)
		{
			return this.liveTypeHandlers.keys().contains(type);
		}

		@Override
		public final synchronized <T> boolean registerTypeHandler(
			final Class<T>                             type                  ,
			final PersistenceTypeHandler<D, ? super T> typeHandlerInitializer
		)
		{
			// (31.03.2020 Paigan)FIXME: priv#187: validate type (Class#isAssignableFrom!)
			
			// put instead of add to allow custom-tailed replacments for native handlers (e.g. divergent TID or logic)
			return this.liveTypeHandlers.put(type, typeHandlerInitializer);
		}

		@Override
		public <T> boolean registerTypeHandler(
			final PersistenceTypeHandler<D, T> typeHandlerInitializer
		)
		{
			return this.registerTypeHandler(
				typeHandlerInitializer.type(),
				typeHandlerInitializer
			);
		}

		@Override
		public synchronized PersistenceCustomTypeHandlerRegistry.Default<D> registerTypeHandlers(
			final XGettingCollection<? extends PersistenceTypeHandler<D, ?>> typeHandlerInitializers
		)
		{
			for(final PersistenceTypeHandler<D, ?> th : typeHandlerInitializers)
			{
				this.registerTypeHandler(th);
			}
			
			return this;
		}
		
		@Override
		public synchronized <T> boolean registerLegacyTypeHandler(
			final PersistenceLegacyTypeHandler<D, T> legacyTypeHandler
		)
		{
			return this.legacyTypeHandlers.add(legacyTypeHandler);
		}
		
		@Override
		public synchronized PersistenceCustomTypeHandlerRegistry<D> registerLegacyTypeHandlers(
			final XGettingCollection<? extends PersistenceLegacyTypeHandler<D, ?>> legacyTypeHandlers
		)
		{
			for(final PersistenceLegacyTypeHandler<D, ?> lth : legacyTypeHandlers)
			{
				this.registerLegacyTypeHandler(lth);
			}
			
			return this;
		}

		@SuppressWarnings("unchecked") // cast type safety guaranteed by management logic
		private <T> PersistenceTypeHandler<D, T> internalLookupTypeHandler(final Class<T> type)
		{
			return (PersistenceTypeHandler<D, T>)this.liveTypeHandlers.get(type);
		}

		@Override
		public <T> PersistenceTypeHandler<D, T> lookupTypeHandler(final Class<T> type)
		{
			return this.internalLookupTypeHandler(type);
		}
		
		@Override
		public <C extends Consumer<? super PersistenceTypeHandler<D, ?>>> C iterateTypeHandlers(final C iterator)
		{
			this.liveTypeHandlers.values().iterate(iterator);
			return iterator;
		}
		
		@Override
		public <C extends Consumer<? super PersistenceLegacyTypeHandler<D, ?>>> C iterateLegacyTypeHandlers(final C iterator)
		{
			return this.legacyTypeHandlers().iterate(iterator);
		}
		
		@Override
		public final XGettingEnum<PersistenceLegacyTypeHandler<D, ?>> legacyTypeHandlers()
		{
			return this.legacyTypeHandlers;
		}

	}

}
