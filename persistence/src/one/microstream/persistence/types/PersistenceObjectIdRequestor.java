package one.microstream.persistence.types;

public interface PersistenceObjectIdRequestor
{
	// always implemented for guaranteed registration
	public void registerGuaranteed(long objectId, Object instance);
	
	// implemented by lazy implementation, no-op otherwise
	public void registerLazyOptional(long objectId, Object instance);

	// implemented by eager implementation, no-op otherwise
	public void registerEagerOptional(long objectId, Object instance);
	
	
	
	public static PersistenceObjectIdRequestor NoOp()
	{
		return new PersistenceObjectIdRequestor.NoOp();
	}
	
	public final class NoOp implements PersistenceObjectIdRequestor
	{

		@Override
		public void registerGuaranteed(final long objectId, final Object instance)
		{
			// no-op
		}

		@Override
		public void registerLazyOptional(final long objectId, final Object instance)
		{
			// no-op
		}

		@Override
		public void registerEagerOptional(final long objectId, final Object instance)
		{
			// no-op
		}
		
	}
	
}
