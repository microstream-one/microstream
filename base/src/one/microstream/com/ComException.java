package one.microstream.com;

import one.microstream.exceptions.BaseException;

public class ComException extends BaseException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ComException()
	{
		super();
	}

	public ComException(final String message)
	{
		super(message);
	}

	public ComException(final Throwable cause)
	{
		super(cause);
	}

	public ComException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public ComException(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}
