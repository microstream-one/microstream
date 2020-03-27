package one.microstream.storage.exceptions;

public class StorageConfigurationNotFoundException extends StorageConfigurationIoException
{
	public StorageConfigurationNotFoundException()
	{
		super();
	}

	public StorageConfigurationNotFoundException(
		String message,
		Throwable cause,
		boolean enableSuppression,
		boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public StorageConfigurationNotFoundException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public StorageConfigurationNotFoundException(String message)
	{
		super(message);
	}

	public StorageConfigurationNotFoundException(Throwable cause)
	{
		super(cause);
	}
	
}
