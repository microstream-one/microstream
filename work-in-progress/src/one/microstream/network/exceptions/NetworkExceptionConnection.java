package one.microstream.network.exceptions;

public class NetworkExceptionConnection extends NetworkException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public NetworkExceptionConnection()
	{
		this(null, null);
	}

	public NetworkExceptionConnection(final String message)
	{
		this(message, null);
	}

	public NetworkExceptionConnection(final Throwable cause)
	{
		this(null, cause);
	}

	public NetworkExceptionConnection(final String message, final Throwable cause)
	{
		this(message, cause, true, true);
	}

	public NetworkExceptionConnection(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}



}
