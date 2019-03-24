package one.microstream.storage.exceptions;

public class StorageExceptionIoReading extends StorageExceptionIo
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageExceptionIoReading()
	{
		super();
	}

	public StorageExceptionIoReading(final String message)
	{
		super(message);
	}

	public StorageExceptionIoReading(final Throwable cause)
	{
		super(cause);
	}

	public StorageExceptionIoReading(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public StorageExceptionIoReading(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}



}
