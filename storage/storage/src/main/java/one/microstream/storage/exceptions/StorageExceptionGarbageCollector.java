package one.microstream.storage.exceptions;

public class StorageExceptionGarbageCollector extends StorageException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageExceptionGarbageCollector()
	{
		super();
	}

	public StorageExceptionGarbageCollector(final String message)
	{
		super(message);
	}

	public StorageExceptionGarbageCollector(final Throwable cause)
	{
		super(cause);
	}

	public StorageExceptionGarbageCollector(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public StorageExceptionGarbageCollector(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}
