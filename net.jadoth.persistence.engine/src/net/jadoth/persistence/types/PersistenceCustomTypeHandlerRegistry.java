package net.jadoth.persistence.types;

import java.util.function.Consumer;

import net.jadoth.collections.HashEnum;
import net.jadoth.collections.HashTable;
import net.jadoth.collections.types.XGettingCollection;

public interface PersistenceCustomTypeHandlerRegistry<M> extends PersistenceTypeHandlerIterable<M>
{
	public <T> boolean registerTypeHandler(PersistenceTypeHandler<M, T> typeHandler);

	public <T> boolean registerTypeHandler(Class<T> type, PersistenceTypeHandler<M, T> typeHandler);
	
	public <T> boolean registerLegacyTypeHandler(PersistenceLegacyTypeHandler<M, T> legacyTypeHandler);
	
	public PersistenceCustomTypeHandlerRegistry<M> registerLegacyTypeHandlers(
		XGettingCollection<? extends PersistenceLegacyTypeHandler<M, ?>> legacyTypeHandlers
	);

	public PersistenceCustomTypeHandlerRegistry<M> registerTypeHandlers(
		XGettingCollection<? extends PersistenceTypeHandler<M, ?>> typeHandlers
	);
	
	public <T> PersistenceTypeHandler<M, T> lookupTypeHandler(Class<T> type);

	public boolean knowsType(Class<?> type);
		
	
	
	public static <M> PersistenceCustomTypeHandlerRegistry.Implementation<M> New()
	{
		return new PersistenceCustomTypeHandlerRegistry.Implementation<>();
	}

	public final class Implementation<M> implements PersistenceCustomTypeHandlerRegistry<M>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////
		
		private final HashTable<Class<?>, PersistenceTypeHandler<M, ?>> mapping            = HashTable.New();
		private final HashEnum<PersistenceLegacyTypeHandler<M, ?>>      legacyTypeHandlers = HashEnum.New() ;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation()
		{
			super();
		}
		


		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public synchronized boolean knowsType(final Class<?> type)
		{
			return this.mapping.keys().contains(type);
		}

		@Override
		public final synchronized <T> boolean registerTypeHandler(
			final Class<T>                     type                  ,
			final PersistenceTypeHandler<M, T> typeHandlerInitializer
		)
		{
			return this.mapping.add(type, typeHandlerInitializer);
		}

		@Override
		public <T> boolean registerTypeHandler(
			final PersistenceTypeHandler<M, T> typeHandlerInitializer
		)
		{
			return this.registerTypeHandler(
				typeHandlerInitializer.type(),
				typeHandlerInitializer
			);
		}

		@Override
		public synchronized PersistenceCustomTypeHandlerRegistry.Implementation<M> registerTypeHandlers(
			final XGettingCollection<? extends PersistenceTypeHandler<M, ?>> typeHandlerInitializers
		)
		{
			for(final PersistenceTypeHandler<M, ?> th : typeHandlerInitializers)
			{
				this.registerTypeHandler(th);
			}
			
			return this;
		}
		
		@Override
		public synchronized <T> boolean registerLegacyTypeHandler(
			final PersistenceLegacyTypeHandler<M, T> legacyTypeHandler
		)
		{
			return this.legacyTypeHandlers.add(legacyTypeHandler);
		}
		
		@Override
		public synchronized PersistenceCustomTypeHandlerRegistry<M> registerLegacyTypeHandlers(
			final XGettingCollection<? extends PersistenceLegacyTypeHandler<M, ?>> legacyTypeHandlers
		)
		{
			for(final PersistenceLegacyTypeHandler<M, ?> lth : legacyTypeHandlers)
			{
				this.registerLegacyTypeHandler(lth);
			}
			
			return this;
		}

		@SuppressWarnings("unchecked") // cast type safety guaranteed by management logic
		private <T> PersistenceTypeHandler<M, T> internalLookupTypeHandler(final Class<T> type)
		{
			return (PersistenceTypeHandler<M, T>)this.mapping.get(type);
		}

		@Override
		public <T> PersistenceTypeHandler<M, T> lookupTypeHandler(final Class<T> type)
		{
			return this.internalLookupTypeHandler(type);
		}
		
		@Override
		public <C extends Consumer<? super PersistenceTypeHandler<M, ?>>> C iterateTypeHandlers(final C iterator)
		{
			this.mapping.values().iterate(iterator);
			return iterator;
		}

	}

}
