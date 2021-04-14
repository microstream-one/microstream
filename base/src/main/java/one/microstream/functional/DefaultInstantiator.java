package one.microstream.functional;

import one.microstream.exceptions.InstantiationRuntimeException;
import one.microstream.reflect.XReflect;

public interface DefaultInstantiator
{
	public <T> T instantiate(Class<T> type) throws InstantiationRuntimeException;
	
		
	
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
			return XReflect.defaultInstantiate(type);
		}
		
	}
	
}
