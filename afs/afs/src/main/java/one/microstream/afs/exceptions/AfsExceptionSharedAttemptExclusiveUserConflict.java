package one.microstream.afs.exceptions;

public class AfsExceptionSharedAttemptExclusiveUserConflict extends AfsExceptionConflict
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AfsExceptionSharedAttemptExclusiveUserConflict()
	{
		super();
	}

	public AfsExceptionSharedAttemptExclusiveUserConflict(final String message)
	{
		super(message);
	}

	public AfsExceptionSharedAttemptExclusiveUserConflict(final Throwable cause)
	{
		super(cause);
	}

	public AfsExceptionSharedAttemptExclusiveUserConflict(final String message, final Throwable cause)
	{
		super(message, cause, true, true);
	}

	public AfsExceptionSharedAttemptExclusiveUserConflict(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}
