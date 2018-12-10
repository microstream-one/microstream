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
	
	
	
	public static <M> PersistenceContextDispatcher.PassThrough<M> PassThrough()
	{
		return new PersistenceContextDispatcher.PassThrough<>();
	}
	
	public static <M> PersistenceContextDispatcher.LocalObjectRegistration<M> LocalObjectRegistration()
	{
		return new PersistenceContextDispatcher.LocalObjectRegistration<>();
	}
	
	public final class PassThrough<M> implements PersistenceContextDispatcher<M>
	{
		PassThrough()
		{
			super();
		}
		
		// once again missing interface stateless instantiation.
	}
	
	public final class LocalObjectRegistration<M> implements PersistenceContextDispatcher<M>
	{
		LocalObjectRegistration()
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
		
		@Override
		public final PersistenceObjectManager dispatchObjectManager(
			final PersistenceObjectManager objectManager
		)
		{
			return objectManager.Clone();
		}
		
	}
	
}
