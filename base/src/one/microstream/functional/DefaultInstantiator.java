package one.microstream.functional;

import static one.microstream.functional.DefaultInstantiator.staticInstantiate;

import java.lang.reflect.Constructor;

import one.microstream.exceptions.InstantiationRuntimeException;
import one.microstream.exceptions.NoSuchMethodRuntimeException;

public interface DefaultInstantiator
{
	public <T> T instantiate(Class<T> type) throws InstantiationRuntimeException;
	
	
	public static <T> T staticInstantiate(final Class<T> type)
		throws NoSuchMethodRuntimeException, InstantiationRuntimeException
	{
		final Constructor<T> defaultConstructor;
		try
		{
			defaultConstructor = type.getConstructor();
		}
		catch(final NoSuchMethodException e)
		{
			// childich checked exceptions ...
			throw new NoSuchMethodRuntimeException(e);
		}
		
		try
		{
			return defaultConstructor.newInstance();
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
	
	
	
	public static DefaultInstantiator.Default Default()
	{
		return new Default();
	}
	
	public final class Default implements DefaultInstantiator
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final <T> T instantiate(final Class<T> type) throws InstantiationRuntimeException
		{
			return staticInstantiate(type);
		}
		
	}
	
}
