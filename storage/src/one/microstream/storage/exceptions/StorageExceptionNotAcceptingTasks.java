package one.microstream.storage.exceptions;

public class StorageExceptionNotAcceptingTasks extends StorageException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageExceptionNotAcceptingTasks()
	{
		super();
	}

	public StorageExceptionNotAcceptingTasks(final String message)
	{
		super(message);
	}

	public StorageExceptionNotAcceptingTasks(final Throwable cause)
	{
		super(cause);
	}

	public StorageExceptionNotAcceptingTasks(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public StorageExceptionNotAcceptingTasks(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}



}
