package one.microstream.storage.exceptions;

public class StorageExceptionIo extends StorageException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageExceptionIo()
	{
		super();
	}

	public StorageExceptionIo(final String message)
	{
		super(message);
	}

	public StorageExceptionIo(final Throwable cause)
	{
		super(cause);
	}

	public StorageExceptionIo(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public StorageExceptionIo(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}



}
