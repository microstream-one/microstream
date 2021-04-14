package one.microstream.persistence.exceptions;

import one.microstream.exceptions.BaseException;

/*
 * XXX check usages of this type, replace by better typed exceptions
 */
public class PersistenceException extends BaseException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceException()
	{
		super();
	}

	public PersistenceException(final String message)
	{
		super(message);
	}

	public PersistenceException(final Throwable cause)
	{
		super(cause);
	}

	public PersistenceException(final String message, final Throwable cause)
	{
		super(message, cause, true, true);
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
