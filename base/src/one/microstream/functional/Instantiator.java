package one.microstream.functional;

import static one.microstream.X.notNull;

import java.lang.reflect.Constructor;

import one.microstream.exceptions.InstantiationRuntimeException;


public interface Instantiator<T>
{
	public T instantiate() throws InstantiationRuntimeException;
	
	
	
	public static <T> Instantiator<T> WrapDefaultConstructor(final Constructor<T> constructor)
	{
		return new WrappingDefaultConstructor<>(
			notNull(constructor)
		);
	}
	
	public final class WrappingDefaultConstructor<T> implements Instantiator<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Constructor<T> constructor;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		WrappingDefaultConstructor(final Constructor<T> constructor)
		{
			super();
			this.constructor = constructor;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		@Override
		public T instantiate() throws InstantiationRuntimeException
		{
			try
			{
				return this.constructor.newInstance();
			}
			catch(final InstantiationException e)
			{
				throw new InstantiationRuntimeException(e);
			}
			catch(final Exception e)
			{
				throw new RuntimeException(e);
			}
		}
		
	}
	
}
