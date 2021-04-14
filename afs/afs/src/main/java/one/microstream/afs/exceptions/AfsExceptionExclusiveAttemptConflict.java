package one.microstream.afs.exceptions;

public class AfsExceptionExclusiveAttemptConflict extends AfsExceptionConflict
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AfsExceptionExclusiveAttemptConflict()
	{
		super();
	}

	public AfsExceptionExclusiveAttemptConflict(final String message)
	{
		super(message);
	}

	public AfsExceptionExclusiveAttemptConflict(final Throwable cause)
	{
		super(cause);
	}

	public AfsExceptionExclusiveAttemptConflict(final String message, final Throwable cause)
	{
		super(message, cause, true, true);
	}

	public AfsExceptionExclusiveAttemptConflict(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}
