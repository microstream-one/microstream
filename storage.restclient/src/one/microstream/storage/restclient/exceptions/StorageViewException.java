package one.microstream.storage.restclient.exceptions;

import one.microstream.exceptions.BaseException;

public class StorageViewException extends BaseException
{
	public StorageViewException()
	{
		super();
	}

	public StorageViewException(
		final String message,
		final Throwable cause,
		final boolean enableSuppression,
		final boolean writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public StorageViewException(
		final String message,
		final Throwable cause
	)
	{
		super(message, cause);
	}

	public StorageViewException(
		final String message
	)
	{
		super(message);
	}

	public StorageViewException(
		final Throwable cause
	)
	{
		super(cause);
	}
	
}
