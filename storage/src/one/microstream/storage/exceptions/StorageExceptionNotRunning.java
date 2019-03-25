package one.microstream.storage.exceptions;

public class StorageExceptionNotRunning extends StorageException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageExceptionNotRunning()
	{
		super();
	}

	public StorageExceptionNotRunning(final String message)
	{
		super(message);
	}

	public StorageExceptionNotRunning(final Throwable cause)
	{
		super(cause);
	}

	public StorageExceptionNotRunning(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public StorageExceptionNotRunning(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}



}
