package one.microstream.persistence.exceptions;

public class PersistenceExceptionTypeHandlerConsistency extends PersistenceExceptionTypeConsistency
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionTypeHandlerConsistency()
	{
		this(null, null);
	}

	public PersistenceExceptionTypeHandlerConsistency(final String message)
	{
		this(message, null);
	}

	public PersistenceExceptionTypeHandlerConsistency(final Throwable cause)
	{
		this(null, cause);
	}

	public PersistenceExceptionTypeHandlerConsistency(final String message, final Throwable cause)
	{
		this(message, cause, true, true);
	}

	public PersistenceExceptionTypeHandlerConsistency(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}



}
