package one.microstream.memory.android;

import one.microstream.exceptions.InstantiationRuntimeException;
import one.microstream.functional.DefaultInstantiator;
import sun.misc.Unsafe;


public final class AndroidInstantiator implements DefaultInstantiator
{
	private static final Unsafe UNSAFE = Unsafe.getUnsafe();

	@SuppressWarnings("unchecked") // cast is safe as the passed type IS the type T.
	@Override
	public final <T> T instantiate(final Class<T> type) throws InstantiationRuntimeException
	{
		try
		{
			return (T)UNSAFE.allocateInstance(type);
		}
		catch(final Exception e)
		{
			// android's Unsafe#allocateInstance exception signature does not match the expectation.
			throw new RuntimeException(e);
		}
	}
	
}