package net.jadoth.persistence.exceptions;

public class PersistenceExceptionTypeConsistency extends PersistenceException
{
	/* note:
	 * would actually have to be a Swizzle TypeConsistency Exception ("as well").
	 * However as exceptions in java are no proper types (interfaces) but mere implementations (classes),
	 * no multiple type inheritance is possible.
	 * As a consequence, no proper complex exception type hierarchy can be constructed in Java.
	 *
	 * Maybe a Java-dot-h interface paradigm has to be crated for exception hierarchies as well.
	 * Problem is, that catch blocks will then have to be cluttered up by tons of instanceof as a workaround
	 * for the missing language feature.
	 * On the bright side, proper architecture turns out to always catch Throwable anyway and then
	 * rethrow / analyse / react accordingly.
	 */

	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

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
