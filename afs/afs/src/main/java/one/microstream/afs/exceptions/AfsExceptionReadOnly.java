package one.microstream.afs.exceptions;

public class AfsExceptionReadOnly extends AfsException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AfsExceptionReadOnly()
	{
		super();
	}

	public AfsExceptionReadOnly(final String message)
	{
		super(message);
	}

	public AfsExceptionReadOnly(final Throwable cause)
	{
		super(cause);
	}

	public AfsExceptionReadOnly(final String message, final Throwable cause)
	{
		super(message, cause, true, true);
	}

	public AfsExceptionReadOnly(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}
