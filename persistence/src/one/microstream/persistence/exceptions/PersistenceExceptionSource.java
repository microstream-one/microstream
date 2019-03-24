package one.microstream.persistence.exceptions;

public class PersistenceExceptionSource extends PersistenceException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionSource()
	{
		super();
	}

	public PersistenceExceptionSource(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public PersistenceExceptionSource(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public PersistenceExceptionSource(final String message)
	{
		super(message);
	}

	public PersistenceExceptionSource(final Throwable cause)
	{
		super(cause);
	}



}
