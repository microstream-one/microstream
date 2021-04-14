package one.microstream.storage.exceptions;

public class StorageExceptionIoWritingTypeDefinitions extends StorageExceptionIoWriting
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageExceptionIoWritingTypeDefinitions()
	{
		super();
	}

	public StorageExceptionIoWritingTypeDefinitions(final String message)
	{
		super(message);
	}

	public StorageExceptionIoWritingTypeDefinitions(final Throwable cause)
	{
		super(cause);
	}

	public StorageExceptionIoWritingTypeDefinitions(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public StorageExceptionIoWritingTypeDefinitions(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}



}
