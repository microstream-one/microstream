package one.microstream.storage.exceptions;

public class StorageConfigurationIoException extends StorageConfigurationException
{
	public StorageConfigurationIoException()
	{
		super();
	}

	public StorageConfigurationIoException(
		String message,
		Throwable cause,
		boolean enableSuppression,
		boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public StorageConfigurationIoException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public StorageConfigurationIoException(String message)
	{
		super(message);
	}

	public StorageConfigurationIoException(Throwable cause)
	{
		super(cause);
	}
	
}
