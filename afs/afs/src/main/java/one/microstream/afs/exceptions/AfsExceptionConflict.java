package one.microstream.afs.exceptions;

public class AfsExceptionConflict extends AfsException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AfsExceptionConflict()
	{
		super();
	}

	public AfsExceptionConflict(final String message)
	{
		super(message);
	}

	public AfsExceptionConflict(final Throwable cause)
	{
		super(cause);
	}

	public AfsExceptionConflict(final String message, final Throwable cause)
	{
		super(message, cause, true, true);
	}

	public AfsExceptionConflict(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}
