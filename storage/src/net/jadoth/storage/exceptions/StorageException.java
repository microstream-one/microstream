package net.jadoth.storage.exceptions;

import net.jadoth.exceptions.BaseException;

public class StorageException extends BaseException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public StorageException()
	{
		super();
	}

	public StorageException(final String message)
	{
		super(message);
	}

	public StorageException(final Throwable cause)
	{
		super(cause);
	}

	public StorageException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public StorageException(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
