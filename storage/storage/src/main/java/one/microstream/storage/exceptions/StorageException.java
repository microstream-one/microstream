package one.microstream.storage.exceptions;

import one.microstream.exceptions.BaseException;

/*
 * XXX check usages of this type, replace by better typed exceptions
 */
public class StorageException extends BaseException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

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
