package one.microstream.persistence.binary.exceptions;

import one.microstream.persistence.exceptions.PersistenceException;

/*
 * XXX check usages of this type, replace by better typed exceptions
 */
public class BinaryPersistenceException extends PersistenceException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryPersistenceException()
	{
		this(null, null);
	}

	public BinaryPersistenceException(final String message)
	{
		this(message, null);
	}

	public BinaryPersistenceException(final Throwable cause)
	{
		this(null, cause);
	}

	public BinaryPersistenceException(final String message, final Throwable cause)
	{
		this(message, cause, true, true);
	}

	public BinaryPersistenceException(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
