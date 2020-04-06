package one.microstream.storage.exceptions;

import one.microstream.exceptions.BaseException;

public class StorageConfigurationException extends BaseException
{
	public StorageConfigurationException()
	{
		super();
	}

	public StorageConfigurationException(
		String message,
		Throwable cause,
		boolean enableSuppression,
		boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public StorageConfigurationException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public StorageConfigurationException(String message)
	{
		super(message);
	}

	public StorageConfigurationException(Throwable cause)
	{
		super(cause);
	}
}
