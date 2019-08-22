package one.microstream.storage.types;

import one.microstream.memory.XMemory;
import one.microstream.meta.XDebug;


// (22.06.2013 TM)NOTE: DEBUG class should be removed at some point.
/* (22.08.2019 TM)NOTE: for now, it's deactivated by default.
 * Although the println calls should really be removed or replaced
 * by a logging mechanism at some point ...
 */
public final class DebugStorage
{
	private static final boolean ENABLED = false;

	private static void internalPrintln(final String s)
	{
		if(ENABLED)
		{
			XDebug.println(s, 2);
		}
	}

	// (22.08.2019 TM)TODO: remove or replace all calls to DebugStorage#println and then the class itself.
	public static final void println(final String s)
	{
		internalPrintln(s);
	}

	public static final byte[] readMemoryRange(final long address, final int length)
	{
		final byte[] array = new byte[length];
		XMemory.copyRangeToArray(address, array);
		return array;
	}

	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException
	 */
	private DebugStorage()
	{
		// static only
		throw new UnsupportedOperationException();
	}

}
