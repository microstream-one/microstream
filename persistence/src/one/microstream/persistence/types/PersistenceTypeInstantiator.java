package one.microstream.persistence.types;

import static one.microstream.X.notNull;

@FunctionalInterface
public interface PersistenceTypeInstantiator<M, T>
{
	public T instantiate(M medium);
	
	
	
	public static <T, M> PersistenceTypeInstantiator<M, T> New(
		final Class<T>                   type                 ,
		final PersistenceInstantiator<M> universalInstantiator
	)
	{
		return new PersistenceTypeInstantiator.Default<>(
			notNull(type),
			notNull(universalInstantiator)
		);
	}
	
	public final class Default<M, T> implements PersistenceTypeInstantiator<M, T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Class<T>                   type                 ;
		private final PersistenceInstantiator<M> universalInstantiator;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final Class<T>                   type                 ,
			final PersistenceInstantiator<M> universalInstantiator
		)
		{
			super();
			this.type                  = type                 ;
			this.universalInstantiator = universalInstantiator;
		}


		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public T instantiate(final M medium)
		{
			return this.universalInstantiator.instantiate(this.type, medium);
		}
		
	}
	
}
