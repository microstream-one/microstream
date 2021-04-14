package one.microstream.persistence.types;

import one.microstream.exceptions.InstantiationRuntimeException;
import one.microstream.memory.XMemory;


public interface PersistenceInstantiator<D>
{
	public <T> T instantiate(Class<T> type, D data) throws InstantiationRuntimeException;
		


	public static <T> T instantiateBlank(final Class<T> type)
	{
		return XMemory.instantiateBlank(type);
	}
	
	
	
	public static <D> PersistenceInstantiator<D> New()
	{
		return new PersistenceInstantiator.Default<>();
	}
	
	public final class Default<D> implements PersistenceInstantiator<D>, PersistenceTypeInstantiatorProvider<D>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		Default()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public <T> T instantiate(final Class<T> type, final D data)
			throws InstantiationRuntimeException
		{
			return PersistenceInstantiator.instantiateBlank(type);
		}
		
		@Override
		public <T> PersistenceTypeInstantiator<D, T> provideTypeInstantiator(final Class<T> type)
		{
			return PersistenceTypeInstantiator.New(type, this);
		}
		
	}
	
}
