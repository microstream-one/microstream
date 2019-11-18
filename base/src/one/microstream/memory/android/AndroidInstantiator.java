package one.microstream.memory.android;

import one.microstream.exceptions.InstantiationRuntimeException;
import one.microstream.functional.DefaultInstantiator;
import sun.misc.Unsafe;
import java.lang.reflect.Field;


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
	

    /*
     * If magic values should be represented by constants and constants should be encapsulated by methods
     * like instance fields should, then why use the code and memory detour of constants in the first place?
     * Direct "Constant Methods" are the logical conclusion and they get jitted away, anyway.
     */
    static final String fieldNameUnsafe()
    {
        return "theUnsafe";
    }


    public static final Unsafe getMemoryAccess()
    {
        try
        {
            final Field theUnsafe = Unsafe.class.getDeclaredField(fieldNameUnsafe());
            theUnsafe.setAccessible(true);
            return (Unsafe)theUnsafe.get(null); // static field, no argument needed, may be null (see #get JavaDoc)
        }
        catch(final Exception e)
        {
            throw new Error("Could not obtain access to \"" + fieldNameUnsafe() + "\"", e);
        }
    }

	
}