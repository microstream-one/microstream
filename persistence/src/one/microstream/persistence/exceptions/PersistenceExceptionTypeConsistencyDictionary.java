package one.microstream.persistence.exceptions;

public class PersistenceExceptionTypeConsistencyDictionary extends PersistenceExceptionTypeConsistency
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionTypeConsistencyDictionary()
	{
		this(null, null);
	}

	public PersistenceExceptionTypeConsistencyDictionary(final String message)
	{
		this(message, null);
	}

	public PersistenceExceptionTypeConsistencyDictionary(final Throwable cause)
	{
		this(null, cause);
	}

	public PersistenceExceptionTypeConsistencyDictionary(final String message, final Throwable cause)
	{
		this(message, cause, true, true);
	}

	public PersistenceExceptionTypeConsistencyDictionary(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}



}
