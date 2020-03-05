package one.microstream.persistence.types;

import static one.microstream.X.notNull;

@FunctionalInterface
public interface PersistenceTypeInstantiator<D, T>
{
	public T instantiate(D data);
	
	
	
	public static <T, D> PersistenceTypeInstantiator<D, T> New(final Class<T> type)
	{
		return New(type, PersistenceInstantiator.New());
	}
	
	public static <T, D> PersistenceTypeInstantiator<D, T> New(
		final Class<T>                   type                 ,
		final PersistenceInstantiator<D> universalInstantiator
	)
	{
		return new PersistenceTypeInstantiator.Default<>(
			notNull(type),
			notNull(universalInstantiator)
		);
	}
	
	public final class Default<D, T> implements PersistenceTypeInstantiator<D, T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Class<T>                   type                 ;
		private final PersistenceInstantiator<D> universalInstantiator;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final Class<T>                   type                 ,
			final PersistenceInstantiator<D> universalInstantiator
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
		public T instantiate(final D data)
		{
			return this.universalInstantiator.instantiate(this.type, data);
		}
		
	}
	
}
