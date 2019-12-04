package one.microstream.memory.android;

import one.microstream.exceptions.InstantiationRuntimeException;
import one.microstream.functional.DefaultInstantiator;


public final class AndroidInstantiatorBlank implements DefaultInstantiator
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static final AndroidInstantiatorBlank New()
	{
		return new AndroidInstantiatorBlank();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	AndroidInstantiatorBlank()
	{
		super();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public final <T> T instantiate(final Class<T> type) throws InstantiationRuntimeException
	{
		return AndroidInternals.instantiateBlank(type);
	}
		
}
