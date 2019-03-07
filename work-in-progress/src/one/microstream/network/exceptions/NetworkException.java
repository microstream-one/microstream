package one.microstream.network.exceptions;

public class NetworkException extends RuntimeException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public NetworkException()
	{
		this(null, null);
	}

	public NetworkException(final String message)
	{
		this(message, null);
	}

	public NetworkException(final Throwable cause)
	{
		this(null, cause);
	}

	public NetworkException(final String message, final Throwable cause)
	{
		this(message, cause, true, true);
	}

	public NetworkException(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}



}
