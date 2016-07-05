package net.jadoth.swizzling.exceptions;

public class SwizzleException extends RuntimeException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public SwizzleException()
	{
		super();
	}

	public SwizzleException(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public SwizzleException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public SwizzleException(final String message)
	{
		super(message);
	}

	public SwizzleException(final Throwable cause)
	{
		super(cause);
	}



}
