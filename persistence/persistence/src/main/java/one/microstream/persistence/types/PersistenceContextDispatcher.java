package one.microstream.persistence.types;

public interface PersistenceContextDispatcher<D>
{
	// loading //
	
	public default PersistenceTypeHandlerLookup<D> dispatchTypeHandlerLookup(
		final PersistenceTypeHandlerLookup<D> typeHandlerLookup
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
	
	public default PersistenceTypeHandlerManager<D> dispatchTypeHandlerManager(
		final PersistenceTypeHandlerManager<D> typeHandlerManager
	)
	{
		return typeHandlerManager;
	}
	
	public default PersistenceObjectManager<D> dispatchObjectManager(
		final PersistenceObjectManager<D> objectManager
	)
	{
		return objectManager;
	}
	
	
	
	public static <D> PersistenceContextDispatcher.PassThrough<D> PassThrough()
	{
		return new PersistenceContextDispatcher.PassThrough<>();
	}
	
	public static <D> PersistenceContextDispatcher.LocalObjectRegistration<D> LocalObjectRegistration()
	{
		return new PersistenceContextDispatcher.LocalObjectRegistration<>();
	}
	
	public final class PassThrough<D> implements PersistenceContextDispatcher<D>
	{
		PassThrough()
		{
			super();
		}
		
		// once again missing interface stateless instantiation.
	}
	
	public final class LocalObjectRegistration<D> implements PersistenceContextDispatcher<D>
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
		public final PersistenceObjectManager<D> dispatchObjectManager(
			final PersistenceObjectManager<D> objectManager
		)
		{
			return objectManager.Clone();
		}
		
	}
	
}
