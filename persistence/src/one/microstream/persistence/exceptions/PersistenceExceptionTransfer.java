package one.microstream.persistence.exceptions;

public class PersistenceExceptionTransfer extends PersistenceException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionTransfer()
	{
		this(null, null);
	}

	public PersistenceExceptionTransfer(final String message)
	{
		this(message, null);
	}

	public PersistenceExceptionTransfer(final Throwable cause)
	{
		this(null, cause);
	}

	public PersistenceExceptionTransfer(final String message, final Throwable cause)
	{
		this(message, cause, true, true);
	}

	public PersistenceExceptionTransfer(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}



}
