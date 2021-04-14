package one.microstream.afs.exceptions;

public class AfsExceptionUnresolvableRoot extends AfsException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AfsExceptionUnresolvableRoot()
	{
		super();
	}

	public AfsExceptionUnresolvableRoot(final String message)
	{
		super(message);
	}

	public AfsExceptionUnresolvableRoot(final Throwable cause)
	{
		super(cause);
	}

	public AfsExceptionUnresolvableRoot(final String message, final Throwable cause)
	{
		super(message, cause, true, true);
	}

	public AfsExceptionUnresolvableRoot(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}
