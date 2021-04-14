package one.microstream.persistence.exceptions;

public class PersistenceExceptionTypeConsistencyDictionaryResolve extends PersistenceExceptionTypeConsistencyDictionary
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionTypeConsistencyDictionaryResolve()
	{
		this(null, null);
	}

	public PersistenceExceptionTypeConsistencyDictionaryResolve(final String message)
	{
		this(message, null);
	}

	public PersistenceExceptionTypeConsistencyDictionaryResolve(final Throwable cause)
	{
		this(null, cause);
	}

	public PersistenceExceptionTypeConsistencyDictionaryResolve(final String message, final Throwable cause)
	{
		this(message, cause, true, true);
	}

	public PersistenceExceptionTypeConsistencyDictionaryResolve(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}



}
