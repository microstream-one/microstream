package one.microstream.persistence.binary.exceptions;

public class BinaryPersistenceExceptionState extends BinaryPersistenceException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryPersistenceExceptionState()
	{
		this(null, null);
	}

	public BinaryPersistenceExceptionState(final String message)
	{
		this(message, null);
	}

	public BinaryPersistenceExceptionState(final Throwable cause)
	{
		this(null, cause);
	}

	public BinaryPersistenceExceptionState(final String message, final Throwable cause)
	{
		this(message, cause, true, true);
	}

	public BinaryPersistenceExceptionState(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}



}
