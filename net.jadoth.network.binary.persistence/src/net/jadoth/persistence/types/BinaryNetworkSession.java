package net.jadoth.persistence.types;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import net.jadoth.exceptions.IORuntimeException;
import net.jadoth.network.types.NetworkUserSession;
import net.jadoth.network.types.NetworkUserSessionManager;
import net.jadoth.persistence.binary.types.Binary;

public class BinaryNetworkSession<U>
extends NetworkUserSession.AbstractImplementation<U, Binary, BinaryNetworkSession<U>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	// (15.04.2016 TM)TODO: those values must be configurable
	private static final int TIMEOUT            = 1000; // in milliseconds
	private static final int MAX_MESSAGE_LENGTH = 8192; // in bytes, not chars!



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final long sessionId;

	private final ByteBuffer buffer = ByteBuffer.allocateDirect(MAX_MESSAGE_LENGTH);

	// (05.11.2012 TM)TODO: BinaryNetworkSession#needsProcessing: maybe can be consolidated away
	private volatile boolean awaitingProcessing;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryNetworkSession(
		final NetworkUserSessionManager<U, BinaryNetworkSession<U>> sessionManager,
		final U                 user      ,
		final SocketChannel     connection,
		final long              sessionId
	)
	{
		super(sessionManager, user, connection);
		this.sessionId = sessionId;
		this.reset();
	}



	///////////////////////////////////////////////////////////////////////////
	// getters          //
	/////////////////////

	public long sessionId()
	{
		return this.sessionId;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	protected void reset()
	{
		LogicNetworkBinaryPersistence.resetBuffer(this.buffer); // must be reset before the awaiting flag
		this.awaitingProcessing = false;
	}

	public void sendMessage(final String message) throws IORuntimeException
	{
		// (06.12.2012)FIXME: TODO
//		try
//		{
//			LogicSimpleNetwork.sendString(message, this.channel());
//		}
//		catch(final IOException e)
//		{
//			throw new IORuntimeException(e);
//		}
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	protected Binary internalReadMessage()
	{
		/* this method only gets called if the session signaled a need for processing.
		 * That means the buffer contains at least one header byte collected by the check.
		 */
		try
		{
			return LogicNetworkBinaryPersistence.completeReadChunk(
				this.channel()    ,
				this.buffer       ,
				TIMEOUT           ,
				MAX_MESSAGE_LENGTH
			);
		}
		catch(final IOException e)
		{
			throw new IORuntimeException(e);
		}
		finally
		{
			this.reset(); // reset on success or failure for next use in check
		}
	}

	@Override
	protected boolean internalNeedsProcessing()
	{
		// prevent multiple initiations of message processing before processing begins
		if(this.awaitingProcessing)
		{
			return false;
		}

		// check if there are new pending bytes in the channel (buffer is guaranteed to be
		try
		{
			if(this.channel().read(this.buffer) > 0)
			{
				this.touch();
				this.awaitingProcessing = true;
				return true;
			}
			return false;
		}
		catch(final IOException e)
		{
			this.reset(); // in case of failure reset for the next check
			throw new IORuntimeException(e);
		}
	}

	@Override
	public String toString()
	{
		return this.getClass().getSimpleName() + "(ID=" + this.sessionId + ")";
	}

}
