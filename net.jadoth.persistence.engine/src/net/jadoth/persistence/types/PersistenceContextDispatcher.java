package net.jadoth.persistence.types;

public interface PersistenceContextDispatcher<M>
{
	// loading //
	
	public default PersistenceTypeHandlerLookup<M> dispatchTypeHandlerLookup(
		final PersistenceTypeHandlerLookup<M> typeHandlerLookup
	)
	{
		return typeHandlerLookup;
	}
	
	public default PersistenceObjectRegistry dispatchObjectRegistry(
		final PersistenceObjectRegistry objectRegistry
	)
	{
		return objectRegistry;
	}
	
	// storing //
	
	public default PersistenceTypeHandlerManager<M> dispatchTypeHandlerManager(
		final PersistenceTypeHandlerManager<M> typeHandlerManager
	)
	{
		return typeHandlerManager;
	}
	
	public default PersistenceObjectManager dispatchObjectManager(
		final PersistenceObjectManager objectManager
	)
	{
		return objectManager;
	}
	
	
	
	public static <M> PersistenceContextDispatcher.NoOp<M> NoOp()
	{
		return new PersistenceContextDispatcher.NoOp<>();
	}
	
	public static <M> PersistenceContextDispatcher.LocalObjectRegistry<M> LocalObjectRegistry()
	{
		return new PersistenceContextDispatcher.LocalObjectRegistry<>();
	}
	
	public final class NoOp<M> implements PersistenceContextDispatcher<M>
	{
		NoOp()
		{
			super();
		}
		
		// once again missing interface stateless instantiation.
		
	}
	
	public final class LocalObjectRegistry<M> implements PersistenceContextDispatcher<M>
	{
		LocalObjectRegistry()
		{
			super();
		}
		
		@Override
		public final PersistenceObjectRegistry dispatchObjectRegistry(
			final PersistenceObjectRegistry objectRegistry
		)
		{
			return objectRegistry.Clone();
		}
		
	}
	
}
