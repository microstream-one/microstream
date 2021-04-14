package one.microstream.storage.exceptions;


public class StorageExceptionInvalidEntityManagerChannelCount extends StorageException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageExceptionInvalidEntityManagerChannelCount()
	{
		super();
	}

	public StorageExceptionInvalidEntityManagerChannelCount(final String message)
	{
		super(message);
	}

	public StorageExceptionInvalidEntityManagerChannelCount(final Throwable cause)
	{
		super(cause);
	}

	public StorageExceptionInvalidEntityManagerChannelCount(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public StorageExceptionInvalidEntityManagerChannelCount(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}



}
