package one.microstream.storage.exceptions;

public class StorageExceptionIoWritingChunk extends StorageExceptionIoWriting
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageExceptionIoWritingChunk()
	{
		super();
	}

	public StorageExceptionIoWritingChunk(final String message)
	{
		super(message);
	}

	public StorageExceptionIoWritingChunk(final Throwable cause)
	{
		super(cause);
	}

	public StorageExceptionIoWritingChunk(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public StorageExceptionIoWritingChunk(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}



}
