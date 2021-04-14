package one.microstream.storage.exceptions;

public class StorageExceptionIncompleteValidation extends StorageException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageExceptionIncompleteValidation()
	{
		super();
	}

	public StorageExceptionIncompleteValidation(final String message)
	{
		super(message);
	}

	public StorageExceptionIncompleteValidation(final Throwable cause)
	{
		super(cause);
	}

	public StorageExceptionIncompleteValidation(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public StorageExceptionIncompleteValidation(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}
