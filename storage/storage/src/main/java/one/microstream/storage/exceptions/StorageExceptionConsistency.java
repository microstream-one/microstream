package one.microstream.storage.exceptions;

public class StorageExceptionConsistency extends StorageException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageExceptionConsistency()
	{
		super();
	}

	public StorageExceptionConsistency(final String message)
	{
		super(message);
	}

	public StorageExceptionConsistency(final Throwable cause)
	{
		super(cause);
	}

	public StorageExceptionConsistency(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public StorageExceptionConsistency(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}
