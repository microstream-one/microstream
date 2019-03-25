package one.microstream.network.exceptions;

public class NetworkExceptionTimeout extends NetworkException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public NetworkExceptionTimeout()
	{
		this(null, null);
	}

	public NetworkExceptionTimeout(final String message)
	{
		this(message, null);
	}

	public NetworkExceptionTimeout(final Throwable cause)
	{
		this(null, cause);
	}

	public NetworkExceptionTimeout(final String message, final Throwable cause)
	{
		this(message, cause, true, true);
	}

	public NetworkExceptionTimeout(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}



}
