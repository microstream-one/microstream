package one.microstream.storage.exceptions;

public class StorageExceptionNotActive extends StorageException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageExceptionNotActive()
	{
		super();
	}

	public StorageExceptionNotActive(final String message)
	{
		super(message);
	}

	public StorageExceptionNotActive(final Throwable cause)
	{
		super(cause);
	}

	public StorageExceptionNotActive(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public StorageExceptionNotActive(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}



}
