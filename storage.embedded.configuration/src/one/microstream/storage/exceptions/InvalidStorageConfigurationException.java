package one.microstream.storage.exceptions;

public class InvalidStorageConfigurationException extends StorageConfigurationException
{
	public InvalidStorageConfigurationException()
	{
		super();
	}

	public InvalidStorageConfigurationException(
		String message,
		Throwable cause,
		boolean enableSuppression,
		boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InvalidStorageConfigurationException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public InvalidStorageConfigurationException(String message)
	{
		super(message);
	}

	public InvalidStorageConfigurationException(Throwable cause)
	{
		super(cause);
	}
	
}
