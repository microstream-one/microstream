package one.microstream.persistence.exceptions;

public class PersistenceExceptionTypeConsistency extends PersistenceException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionTypeConsistency()
	{
		this(null, null);
	}

	public PersistenceExceptionTypeConsistency(final String message)
	{
		this(message, null);
	}

	public PersistenceExceptionTypeConsistency(final Throwable cause)
	{
		this(null, cause);
	}

	public PersistenceExceptionTypeConsistency(final String message, final Throwable cause)
	{
		this(message, cause, true, true);
	}

	public PersistenceExceptionTypeConsistency(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}



}
