package one.microstream.network.simplesession;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import one.microstream.exceptions.IORuntimeException;
import one.microstream.network.types.NetworkUserSession;
import one.microstream.network.types.NetworkUserSessionManager;

public class SimpleSession extends NetworkUserSession.AbstractImplementation<SimpleSessionUser, String, SimpleSession>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	// (15.04.2016 TM)TODO: those values must be configurable
	private static final int TIMEOUT            = 1000; // in milliseconds
	private static final int MAX_MESSAGE_LENGTH = 8192; // in bytes, not chars!



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final long sessionId;

	private final ByteBuffer buffer = ByteBuffer.allocateDirect(MAX_MESSAGE_LENGTH);

	// (05.11.2012 TM)TODO: SimpleSession#needsProcessing: maybe can be consolidated away
	private volatile boolean awaitingProcessing;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public SimpleSession(
		final NetworkUserSessionManager<SimpleSessionUser, SimpleSession> sessionManager,
		final SimpleSessionUser user      ,
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
		LogicSimpleNetwork.resetBuffer(this.buffer); // must be reset before the awaiting flag
		this.awaitingProcessing = false;
	}

	public void sendMessage(final String message) throws IORuntimeException
	{
		try
		{
			LogicSimpleNetwork.sendString(message, this.channel());
		}
		catch(final IOException e)
		{
			throw new IORuntimeException(e);
		}
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	protected String internalReadMessage()
	{
		/* this method only gets called if the session signaled a need for processing.
		 * That means the buffer contains at least one header byte collected by the check.
		 */
		try
		{
			return LogicSimpleNetwork.completeReadString(this.channel(), this.buffer, TIMEOUT, MAX_MESSAGE_LENGTH);
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
