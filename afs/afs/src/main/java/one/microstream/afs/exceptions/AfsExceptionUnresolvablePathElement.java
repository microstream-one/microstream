package one.microstream.afs.exceptions;

public class AfsExceptionUnresolvablePathElement extends AfsException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AfsExceptionUnresolvablePathElement()
	{
		super();
	}

	public AfsExceptionUnresolvablePathElement(final String message)
	{
		super(message);
	}

	public AfsExceptionUnresolvablePathElement(final Throwable cause)
	{
		super(cause);
	}

	public AfsExceptionUnresolvablePathElement(final String message, final Throwable cause)
	{
		super(message, cause, true, true);
	}

	public AfsExceptionUnresolvablePathElement(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}
