/**
 *
 */
package net.jadoth.exceptions;


/**
 * @author Thomas Muenz
 *
 */
public class WrapperRuntimeException extends RuntimeException
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Exception actual;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public WrapperRuntimeException(final Exception actual)
	{
		super();
		this.actual = actual;
		// can't use fillInStackTrace() because of diletantic constructor-intrinsic call in Throwable
		this.setStackTrace(this.actual.getStackTrace());
	}



	///////////////////////////////////////////////////////////////////////////
	// getters          //
	/////////////////////

	public Exception getActual()
	{
		return this.actual;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public synchronized Throwable getCause()
	{
		return this.actual.getCause();
	}

	@Override
	public StackTraceElement[] getStackTrace()
	{
		return this.actual.getStackTrace();
	}

	@Override
	public String getMessage()
	{
		return this.actual.getMessage();
	}

	@Override
	public synchronized Throwable fillInStackTrace()
	{
		return this;
	}



}
