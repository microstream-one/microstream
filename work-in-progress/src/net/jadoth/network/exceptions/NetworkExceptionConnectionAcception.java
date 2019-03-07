package net.jadoth.network.exceptions;

import java.nio.channels.SocketChannel;

public class NetworkExceptionConnectionAcception extends NetworkExceptionConnection
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private final SocketChannel newConnection;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public NetworkExceptionConnectionAcception(final SocketChannel newConnection)
	{
		this(newConnection, null, null);
	}

	public NetworkExceptionConnectionAcception(final SocketChannel newConnection,
		final String message
	)
	{
		this(newConnection, message, null);
	}

	public NetworkExceptionConnectionAcception(final SocketChannel newConnection,
		final Throwable cause
	)
	{
		this(newConnection, null, cause);
	}

	public NetworkExceptionConnectionAcception(final SocketChannel newConnection,
		final String message, final Throwable cause
	)
	{
		this(newConnection, message, cause, true, true);
	}

	public NetworkExceptionConnectionAcception(final SocketChannel newConnection,
		final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.newConnection = newConnection;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters          //
	/////////////////////

	public SocketChannel getNewConnection()
	{
		return this.newConnection;
	}



}
