package one.microstream.storage.exceptions;

public class StorageExceptionIoWriting extends StorageExceptionIo
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageExceptionIoWriting()
	{
		super();
	}

	public StorageExceptionIoWriting(final String message)
	{
		super(message);
	}

	public StorageExceptionIoWriting(final Throwable cause)
	{
		super(cause);
	}

	public StorageExceptionIoWriting(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public StorageExceptionIoWriting(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}



}
