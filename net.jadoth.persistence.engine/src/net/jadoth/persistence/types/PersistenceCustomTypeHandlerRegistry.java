package net.jadoth.persistence.types;

import net.jadoth.collections.HashTable;
import net.jadoth.collections.types.XGettingCollection;

public interface PersistenceCustomTypeHandlerRegistry<M>
{
	public <T> PersistenceCustomTypeHandlerRegistry<M> registerTypeHandler(
		PersistenceTypeHandler<M, ?> typeHandlerInitializer
	);

	public <T> PersistenceCustomTypeHandlerRegistry<M> registerTypeHandler(
		Class<T>                     type            ,
		PersistenceTypeHandler<M, ?> typeHandlerInitializer
	);

	public PersistenceCustomTypeHandlerRegistry<M> registerTypeHandlers(
		XGettingCollection<? extends PersistenceTypeHandler<M, ?>> typeHandlerInitializers
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
		
		private final HashTable<Class<?>, PersistenceTypeHandler<M, ?>> mapping = HashTable.New();

		
		
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
		public boolean knowsType(final Class<?> type)
		{
			return this.mapping.keys().contains(type);
		}

		@Override
		public final <T> PersistenceCustomTypeHandlerRegistry<M> registerTypeHandler(
			final Class<T>                     type                  ,
			final PersistenceTypeHandler<M, ?> typeHandlerInitializer
		)
		{
			this.mapping.put(type, typeHandlerInitializer);
			return this;
		}

		@Override
		public <T> PersistenceCustomTypeHandlerRegistry.Implementation<M> registerTypeHandler(
			final PersistenceTypeHandler<M, ?> typeHandlerInitializer
		)
		{
			this.registerTypeHandler(
				typeHandlerInitializer.type(),
				typeHandlerInitializer
			);
			return this;
		}

		@Override
		public synchronized PersistenceCustomTypeHandlerRegistry.Implementation<M> registerTypeHandlers(
			final XGettingCollection<? extends PersistenceTypeHandler<M, ?>> typeHandlerInitializers
		)
		{
			for(final PersistenceTypeHandler<M, ?> tdi : typeHandlerInitializers)
			{
				this.registerTypeHandler(tdi);
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

	}

}
