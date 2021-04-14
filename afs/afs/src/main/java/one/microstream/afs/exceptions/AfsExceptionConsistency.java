package one.microstream.afs.exceptions;

public class AfsExceptionConsistency extends AfsException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AfsExceptionConsistency()
	{
		super();
	}

	public AfsExceptionConsistency(final String message)
	{
		super(message);
	}

	public AfsExceptionConsistency(final Throwable cause)
	{
		super(cause);
	}

	public AfsExceptionConsistency(final String message, final Throwable cause)
	{
		super(message, cause, true, true);
	}

	public AfsExceptionConsistency(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}
