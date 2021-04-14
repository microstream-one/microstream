/**
 *
 */
package one.microstream.exceptions;


/**
 * Checked exceptions are a badly designed concept that ruin functional programming and seduce to swallow exceptions
 * with a foolish print instead of handling them properly and ignore unchecked exceptions altogether.
 * 
 * 
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
		super(actual);
		this.actual = actual;
		// can't use fillInStackTrace() because of diletantic constructor-intrinsic call in Throwable
//		this.setStackTrace(this.actual.getStackTrace());
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public Exception getActual()
	{
		return this.actual;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

//	@Override
//	public synchronized Throwable getCause()
//	{
//		return this.actual.getCause();
//	}
//
//	@Override
//	public StackTraceElement[] getStackTrace()
//	{
//		return this.actual.getStackTrace();
//	}
//
//	@Override
//	public String getMessage()
//	{
//		return this.actual.getMessage();
//	}
//
//	@Override
//	public synchronized Throwable fillInStackTrace()
//	{
//		return this;
//	}

}
