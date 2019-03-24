package one.microstream.persistence.exceptions;

public class PersistenceExceptionConsistency extends PersistenceException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionConsistency()
	{
		super();
		// (22.03.2013 TM)EXCP: proper exceptions at all call sites of this
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
