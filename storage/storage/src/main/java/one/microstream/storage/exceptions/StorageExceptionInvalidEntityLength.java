package one.microstream.storage.exceptions;

public class StorageExceptionInvalidEntityLength extends StorageException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageExceptionInvalidEntityLength()
	{
		super();
	}

	public StorageExceptionInvalidEntityLength(final String message)
	{
		super(message);
	}

	public StorageExceptionInvalidEntityLength(final Throwable cause)
	{
		super(cause);
	}

	public StorageExceptionInvalidEntityLength(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public StorageExceptionInvalidEntityLength(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}
