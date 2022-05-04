package one.microstream.storage.exceptions;

@SuppressWarnings("serial")
public class StorageExceptionStructureValidation extends StorageException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageExceptionStructureValidation()
	{
		super();
	}

	public StorageExceptionStructureValidation(final String message)
	{
		super(message);
	}

	public StorageExceptionStructureValidation(final Throwable cause)
	{
		super(cause);
	}

	public StorageExceptionStructureValidation(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public StorageExceptionStructureValidation(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
