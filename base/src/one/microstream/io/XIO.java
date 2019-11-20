package one.microstream.io;

import java.io.Closeable;
import java.io.IOException;

import one.microstream.exceptions.IORuntimeException;
import one.microstream.util.UtilStackTrace;

public final class XIO
{
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public static <T> T execute(final IoOperation<T> operation) throws IORuntimeException
	{
		try
		{
			return operation.performOperation();
		}
		catch(final IOException e)
		{
			throw UtilStackTrace.cutStacktraceByOne(new IORuntimeException(e));
		}
	}
	
	public static final <C extends Closeable> C closeSilent(final C closable)
	{
		if(closable != null)
		{
			try
			{
				closable.close();
			}
			catch(final Exception t)
			{
				// sshhh, silence!
			}
		}
		
		return closable;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException
	 */
	private XIO()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
}
