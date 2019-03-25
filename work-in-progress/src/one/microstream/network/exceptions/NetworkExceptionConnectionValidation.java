package one.microstream.network.exceptions;

import java.nio.channels.SocketChannel;

public class NetworkExceptionConnectionValidation extends NetworkExceptionConnectionAcception
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public NetworkExceptionConnectionValidation(final SocketChannel newConnection)
	{
		this(newConnection, null, null);
	}

	public NetworkExceptionConnectionValidation(final SocketChannel newConnection,
		final String message
	)
	{
		this(newConnection, message, null);
	}

	public NetworkExceptionConnectionValidation(final SocketChannel newConnection,
		final Throwable cause
	)
	{
		this(newConnection, null, cause);
	}

	public NetworkExceptionConnectionValidation(final SocketChannel newConnection,
		final String message, final Throwable cause
	)
	{
		this(newConnection, message, cause, true, true);
	}

	public NetworkExceptionConnectionValidation(final SocketChannel newConnection,
		final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace
	)
	{
		super(newConnection, message, cause, enableSuppression, writableStackTrace);
	}



}
