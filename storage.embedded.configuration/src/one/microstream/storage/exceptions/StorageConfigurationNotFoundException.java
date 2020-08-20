package one.microstream.storage.exceptions;

public class StorageConfigurationNotFoundException extends StorageConfigurationIoException
{
	public StorageConfigurationNotFoundException()
	{
		super();
	}

	public StorageConfigurationNotFoundException(
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

	public StorageConfigurationNotFoundException(
		final String    message,
		final Throwable cause
	)
	{
		super(
			message,
			cause
		);
	}

	public StorageConfigurationNotFoundException(
		final String message
	)
	{
		super(message);
	}

	public StorageConfigurationNotFoundException(
		final Throwable cause
	)
	{
		super(cause);
	}
	
}
