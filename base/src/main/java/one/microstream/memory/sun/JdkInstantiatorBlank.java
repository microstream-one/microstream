package one.microstream.memory.sun;

import one.microstream.exceptions.InstantiationRuntimeException;
import one.microstream.functional.DefaultInstantiator;

public final class JdkInstantiatorBlank implements DefaultInstantiator
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static final JdkInstantiatorBlank New()
	{
		return new JdkInstantiatorBlank();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	JdkInstantiatorBlank()
	{
		super();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final <T> T instantiate(final Class<T> type) throws InstantiationRuntimeException
	{
		return JdkInternals.instantiateBlank(type);
	}
	
}
