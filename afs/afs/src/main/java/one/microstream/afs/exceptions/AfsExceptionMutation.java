package one.microstream.afs.exceptions;

public class AfsExceptionMutation extends AfsException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AfsExceptionMutation()
	{
		super();
	}

	public AfsExceptionMutation(final String message)
	{
		super(message);
	}

	public AfsExceptionMutation(final Throwable cause)
	{
		super(cause);
	}

	public AfsExceptionMutation(final String message, final Throwable cause)
	{
		super(message, cause, true, true);
	}

	public AfsExceptionMutation(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}
