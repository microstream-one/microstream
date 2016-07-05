package net.jadoth.swizzling.exceptions;

public class SwizzleExceptionConsistency extends SwizzleException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public SwizzleExceptionConsistency()
	{
		super();
		// (22.03.2013 TM)EXCP: proper exceptions at all call sites of this
	}

	public SwizzleExceptionConsistency(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public SwizzleExceptionConsistency(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public SwizzleExceptionConsistency(final String message)
	{
		super(message);
	}

	public SwizzleExceptionConsistency(final Throwable cause)
	{
		super(cause);
	}



}
