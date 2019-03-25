package one.microstream.persistence.exceptions;

public class PersistenceExceptionTypeHandlerConsistencyProvider extends PersistenceExceptionTypeConsistency
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionTypeHandlerConsistencyProvider()
	{
		this(null, null);
	}

	public PersistenceExceptionTypeHandlerConsistencyProvider(final String message)
	{
		this(message, null);
	}

	public PersistenceExceptionTypeHandlerConsistencyProvider(final Throwable cause)
	{
		this(null, cause);
	}

	public PersistenceExceptionTypeHandlerConsistencyProvider(final String message, final Throwable cause)
	{
		this(message, cause, true, true);
	}

	public PersistenceExceptionTypeHandlerConsistencyProvider(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}



}
