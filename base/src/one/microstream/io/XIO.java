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
	
	public static void execute(final IoOperation operation) throws IORuntimeException
	{
		try
		{
			operation.execute();
		}
		catch(final IOException e)
		{
			throw UtilStackTrace.cutStacktraceByOne(new IORuntimeException(e));
		}
	}
	
	public static <T> T execute(final IoOperationR<T> operation) throws IORuntimeException
	{
		try
		{
			return operation.executeR();
		}
		catch(final IOException e)
		{
			throw UtilStackTrace.cutStacktraceByOne(new IORuntimeException(e));
		}
	}
	
	public static <S> void execute(final IoOperationS<S> operation, final S subject) throws IORuntimeException
	{
		try
		{
			operation.executeS(subject);
		}
		catch(final IOException e)
		{
			throw UtilStackTrace.cutStacktraceByOne(new IORuntimeException(e));
		}
	}
	
	public static <S, R> R execute(final IoOperationSR<S, R> operation, final S subject) throws IORuntimeException
	{
		try
		{
			return operation.executeSR(subject);
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
