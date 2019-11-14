package one.microstream.persistence.types;

import one.microstream.exceptions.InstantiationRuntimeException;
import one.microstream.memory.XMemory;


public interface PersistenceInstantiator<M>
{
	public <T> T instantiate(Class<T> type, M medium) throws InstantiationRuntimeException;
		


	public static <T> T instantiateBlank(final Class<T> type)
	{
		return XMemory.instantiateBlank(type);
	}
	
	
	
	public static <M> PersistenceInstantiator<M> New()
	{
		return new PersistenceInstantiator.Default<>();
	}
	
	public final class Default<M> implements PersistenceInstantiator<M>, PersistenceTypeInstantiatorProvider<M>
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
		public <T> T instantiate(final Class<T> type, final M medium)
			throws InstantiationRuntimeException
		{
			return PersistenceInstantiator.instantiateBlank(type);
		}
		
		@Override
		public <T> PersistenceTypeInstantiator<M, T> provideTypeInstantiator(final Class<T> type)
		{
			return PersistenceTypeInstantiator.New(type, this);
		}
		
	}
	
}
