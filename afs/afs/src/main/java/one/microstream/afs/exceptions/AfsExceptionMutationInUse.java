package one.microstream.afs.exceptions;

public class AfsExceptionMutationInUse extends AfsExceptionMutation
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AfsExceptionMutationInUse()
	{
		super();
	}

	public AfsExceptionMutationInUse(final String message)
	{
		super(message);
	}

	public AfsExceptionMutationInUse(final Throwable cause)
	{
		super(cause);
	}

	public AfsExceptionMutationInUse(final String message, final Throwable cause)
	{
		super(message, cause, true, true);
	}

	public AfsExceptionMutationInUse(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}
