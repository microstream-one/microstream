package one.microstream.persistence.binary.exceptions;

public class BinaryPersistenceExceptionStateArray extends BinaryPersistenceExceptionState
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryPersistenceExceptionStateArray()
	{
		this(null, null);
	}

	public BinaryPersistenceExceptionStateArray(final String message)
	{
		this(message, null);
	}

	public BinaryPersistenceExceptionStateArray(final Throwable cause)
	{
		this(null, cause);
	}

	public BinaryPersistenceExceptionStateArray(final String message, final Throwable cause)
	{
		this(message, cause, true, true);
	}

	public BinaryPersistenceExceptionStateArray(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}



}
