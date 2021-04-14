package one.microstream.storage.configuration;

import one.microstream.exceptions.BaseException;

/**
 * 
 * @deprecated will be removed in a future release
 * @see one.microstream.storage.configuration
 */
@Deprecated
public class StorageConfigurationException extends BaseException
{
	public StorageConfigurationException()
	{
		super();
	}

	public StorageConfigurationException(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(
			message           ,
			cause             ,
			enableSuppression ,
			writableStackTrace
		);
	}

	public StorageConfigurationException(
		final String    message,
		final Throwable cause
	)
	{
		super(
			message,
			cause
		);
	}

	public StorageConfigurationException(
		final String message
	)
	{
		super(message);
	}

	public StorageConfigurationException(
		final Throwable cause
	)
	{
		super(cause);
	}
	
}
