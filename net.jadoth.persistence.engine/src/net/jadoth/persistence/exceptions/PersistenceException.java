package net.jadoth.persistence.exceptions;

public class PersistenceException extends RuntimeException
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
