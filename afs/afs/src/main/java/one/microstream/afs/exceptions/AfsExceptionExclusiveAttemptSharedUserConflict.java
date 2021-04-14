package one.microstream.afs.exceptions;

public class AfsExceptionExclusiveAttemptSharedUserConflict extends AfsExceptionConflict
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AfsExceptionExclusiveAttemptSharedUserConflict()
	{
		super();
	}

	public AfsExceptionExclusiveAttemptSharedUserConflict(final String message)
	{
		super(message);
	}

	public AfsExceptionExclusiveAttemptSharedUserConflict(final Throwable cause)
	{
		super(cause);
	}

	public AfsExceptionExclusiveAttemptSharedUserConflict(final String message, final Throwable cause)
	{
		super(message, cause, true, true);
	}

	public AfsExceptionExclusiveAttemptSharedUserConflict(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}
