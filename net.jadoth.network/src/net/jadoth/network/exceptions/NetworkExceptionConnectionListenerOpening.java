package net.jadoth.network.exceptions;

import java.nio.channels.ServerSocketChannel;

public class NetworkExceptionConnectionListenerOpening extends NetworkExceptionConnection
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private final ServerSocketChannel connectionSocket;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public NetworkExceptionConnectionListenerOpening(final ServerSocketChannel connectionSocket)
	{
		this(connectionSocket, null, null);
	}

	public NetworkExceptionConnectionListenerOpening(final ServerSocketChannel connectionSocket,
		final String message
		)
	{
		this(connectionSocket, message, null);
	}

	public NetworkExceptionConnectionListenerOpening(final ServerSocketChannel connectionSocket,
		final Throwable cause
		)
	{
		this(connectionSocket, null, cause);
	}

	public NetworkExceptionConnectionListenerOpening(final ServerSocketChannel connectionSocket,
		final String message, final Throwable cause
		)
	{
		this(connectionSocket, message, cause, true, true);
	}

	public NetworkExceptionConnectionListenerOpening(final ServerSocketChannel connectionSocket,
		final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace
		)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.connectionSocket = connectionSocket;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters          //
	/////////////////////

	public ServerSocketChannel getConnectionSocket()
	{
		return this.connectionSocket;
	}



}
