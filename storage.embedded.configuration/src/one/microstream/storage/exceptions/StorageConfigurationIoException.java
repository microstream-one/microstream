package one.microstream.storage.exceptions;

public class StorageConfigurationIoException extends StorageConfigurationException
{
	public StorageConfigurationIoException()
	{
		super();
	}

	public StorageConfigurationIoException(
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

	public StorageConfigurationIoException(
		final String    message,
		final Throwable cause
	)
	{
		super(
			message,
			cause
		);
	}

	public StorageConfigurationIoException(
		final String message
	)
	{
		super(message);
	}

	public StorageConfigurationIoException(
		final Throwable cause
	)
	{
		super(cause);
	}
	
}
