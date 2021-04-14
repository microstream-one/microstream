package one.microstream.persistence.exceptions;

/*
 * XXX check usages of this type, replace by better typed exceptions
 */
public class PersistenceExceptionConsistency extends PersistenceException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionConsistency()
	{
		super();
	}

	public PersistenceExceptionConsistency(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public PersistenceExceptionConsistency(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public PersistenceExceptionConsistency(final String message)
	{
		super(message);
	}

	public PersistenceExceptionConsistency(final Throwable cause)
	{
		super(cause);
	}



}
