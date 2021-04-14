package one.microstream.persistence.types;

public interface PersistenceObjectIdRequestor<D>
{
	// always implemented for guaranteed registration
	public <T> void registerGuaranteed(
		long                         objectId       ,
		T                            instance       ,
		PersistenceTypeHandler<D, T> optionalHandler
	);
	
	// implemented by lazy implementation, no-op otherwise
	public <T> void registerLazyOptional(
		long                         objectId       ,
		T                            instance       ,
		PersistenceTypeHandler<D, T> optionalHandler
	);

	// implemented by eager implementation, no-op otherwise
	public <T> void registerEagerOptional(
		long                         objectId       ,
		T                            instance       ,
		PersistenceTypeHandler<D, T> optionalHandler
	);
	
	
	
	public static <D> PersistenceObjectIdRequestor<D> NoOp()
	{
		return new PersistenceObjectIdRequestor.NoOp<>();
	}
	
	public final class NoOp<D> implements PersistenceObjectIdRequestor<D>
	{

		@Override
		public <T> void registerGuaranteed(
			final long                         objectId       ,
			final T                            instance       ,
			final PersistenceTypeHandler<D, T> optionalHandler
		)
		{
			// no-op
		}

		@Override
		public <T> void registerLazyOptional(
			final long                         objectId       ,
			final T                            instance       ,
			final PersistenceTypeHandler<D, T> optionalHandler
		)
		{
			// no-op
		}

		@Override
		public <T> void registerEagerOptional(
			final long                         objectId       ,
			final T                            instance       ,
			final PersistenceTypeHandler<D, T> optionalHandler
		)
		{
			// no-op
		}
		
	}
	
}
