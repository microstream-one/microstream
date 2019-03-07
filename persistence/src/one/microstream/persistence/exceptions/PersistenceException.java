package one.microstream.persistence.exceptions;

import one.microstream.exceptions.BaseException;

public class PersistenceException extends BaseException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public PersistenceException()
	{
		this(null, null);
	}

	public PersistenceException(final String message)
	{
		this(message, null);
	}

	public PersistenceException(final Throwable cause)
	{
		this(null, cause);
	}

	public PersistenceException(final String message, final Throwable cause)
	{
		this(message, cause, true, true);
	}

	public PersistenceException(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}
