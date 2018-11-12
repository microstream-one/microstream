package net.jadoth.com;

import net.jadoth.exceptions.BaseException;

public class ComException extends BaseException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public ComException()
	{
		this(null, null);
	}

	public ComException(final String message)
	{
		this(message, null);
	}

	public ComException(final Throwable cause)
	{
		this(null, cause);
	}

	public ComException(final String message, final Throwable cause)
	{
		this(message, cause, true, true);
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
