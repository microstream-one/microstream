package net.jadoth.util;

import java.lang.reflect.Field;

import net.jadoth.memory.Memory;
//CHECKSTYLE.OFF: IllegalImport: low-level system tools are required for high performance low-level operations
import sun.misc.Unsafe;
//CHECKSTYLE.ON: IllegalImport

/**
 * Non-memory-related vm-level operations like unchecked {@link Throwable} throwing or lock handling.
 *
 * @see Memory
 *
 * @author Thomas Muenz
 */
public final class XVM
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	private static final Unsafe VM = (Unsafe)getSystemInstance();



	///////////////////////////////////////////////////////////////////////////
	// static methods    //
	/////////////////////

	// return type not specified to avoid public API dependencies to sun implementation details
	public static final Object getSystemInstance()
	{
		// all that clumsy detour ... x_x
		if(XVM.class.getClassLoader() == null)
		{
			return Unsafe.getUnsafe(); // Not on bootclasspath
		}
		try
		{
			final Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
			theUnsafe.setAccessible(true);
			return theUnsafe.get(XVM.class);
		}
		catch(final Exception e)
		{
			throw new Error("Could not obtain access to sun.misc.Unsafe", e);
		}
	}

	public static final void throwUnchecked(final Throwable t)
	{
		VM.throwException(t);
	}



	private XVM()
	{
		// static only
		throw new UnsupportedOperationException();
	}
}
