package one.microstream.persistence.binary.exceptions;

public class BinaryPersistenceExceptionUnhandledCollectionType extends BinaryPersistenceException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryPersistenceExceptionUnhandledCollectionType()
	{
		this(null, null);
	}

	public BinaryPersistenceExceptionUnhandledCollectionType(final String message)
	{
		this(message, null);
	}

	public BinaryPersistenceExceptionUnhandledCollectionType(final Throwable cause)
	{
		this(null, cause);
	}

	public BinaryPersistenceExceptionUnhandledCollectionType(final String message, final Throwable cause)
	{
		this(message, cause, true, true);
	}

	public BinaryPersistenceExceptionUnhandledCollectionType(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}



}
