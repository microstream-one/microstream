package one.microstream.storage.exceptions;

import one.microstream.storage.exceptions.StorageException;

public class StorageExceptionInvalidConfiguration extends StorageException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public StorageExceptionInvalidConfiguration()
	{
		super();
	}

	public StorageExceptionInvalidConfiguration(final String message)
	{
		super(message);
	}

	public StorageExceptionInvalidConfiguration(final Throwable cause)
	{
		super(cause);
	}

	public StorageExceptionInvalidConfiguration(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public StorageExceptionInvalidConfiguration(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}



}
