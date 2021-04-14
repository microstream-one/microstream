package one.microstream.afs.exceptions;

public class AfsExceptionRetiredFile extends AfsException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AfsExceptionRetiredFile()
	{
		super();
	}

	public AfsExceptionRetiredFile(final String message)
	{
		super(message);
	}

	public AfsExceptionRetiredFile(final Throwable cause)
	{
		super(cause);
	}

	public AfsExceptionRetiredFile(final String message, final Throwable cause)
	{
		super(message, cause, true, true);
	}

	public AfsExceptionRetiredFile(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}
