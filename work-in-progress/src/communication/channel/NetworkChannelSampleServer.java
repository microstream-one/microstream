package communication.channel;

import static one.microstream.concurrency.XThreads.start;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

/* Example taken from
 * http://www.javaworld.com/javaworld/jw-04-2003/jw-0411-select.html
 * and cleaned up, simplified and beautified the ridiculously crappy code
 */
public class NetworkChannelSampleServer implements Runnable
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	protected static final boolean isAccept(final SelectionKey key)
	{
		return (key.readyOps() & SelectionKey.OP_ACCEPT) != 0;
	}

	protected static final boolean isRead(final SelectionKey key)
	{
		return (key.readyOps() & SelectionKey.OP_READ) != 0;
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	// The port to listen on
	private final InetSocketAddress address;


	private ServerSocketChannel channel ;
	private ServerSocket        socket  ;
	private Selector            selector;

	// A pre-allocated buffer for processing the received data
	private final transient ByteBuffer buffer = ByteBuffer.allocate(16384);



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public NetworkChannelSampleServer(final int port)
	{
		super();
		this.address = new InetSocketAddress(port);
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	public NetworkChannelSampleServer initialize()
	{
		try
		{
			// Instead of creating a ServerSocket, create a ServerSocketChannel
			this.channel = ServerSocketChannel.open();
			this.channel.configureBlocking(false); // non-blocking for use with selector

			// Get the Socket connected to this channel, and bind it to the listening port
			this.socket = this.channel.socket();
			this.socket.bind(this.address);

			// Create a new Selector for selecting
			this.selector = Selector.open();

			// Register the ServerSocketChannel to listen for incoming connections
			this.channel.register(this.selector, SelectionKey.OP_ACCEPT);
		}
		catch(final Exception t)
		{
			throw new NetworkException("Error while initializing server", t);
		}
		System.out.println("Listening on port "+this.address.getPort());
		return this;
	}

	protected void receive()
	{
		// Get the keys corresponding to the activity that has been detected and process them one by one
		final Set<SelectionKey> keys = this.selector.selectedKeys();
		/* (01.09.2012)FIXME: Selector implementation
		 * Stupid iterator instance of stupid JDK collection should be replaced
		 * Selector implementation with tailored XProcessingSet implementation is needed
		 */
		for(final SelectionKey key : keys)
		{
			this.processKey(key);
		}
		keys.clear(); // remove the processed keys
	}

	protected void processKey(final SelectionKey key)
	{
		if(isRead(key))
		{
			this.readData(key);         // case data to read (common case, checked first)
		}
		else if(isAccept(key)) {
			this.acceptNewConnection(); // case incoming connection
		}
	}

	protected void acceptNewConnection()
	{
		System.out.println("Accepting new connection...");
		try
		{
			final Socket s = this.socket.accept();
			System.out.println("Got connection from "+s);

			final SocketChannel sc = s.getChannel();
			sc.configureBlocking(false); // non-blocking for use with selector

			// Register it with the selector, for reading
			sc.register(this.selector, SelectionKey.OP_READ);
		}
		catch(final Exception t)
		{
			// (01.09.2012)XXX: on-error cleanup for this socket?
			throw new NetworkException("Error while accepting connection", t);
		}
	}

	protected void readData(final SelectionKey key)
	{
		try(SocketChannel sc = (SocketChannel)key.channel())
		{
			// It's incoming data on a connection, so  process it
			if(!this.processInput(sc))
			{
				// If the connection is dead, then remove it from the selector and close it
				key.cancel();
				try
				{
					sc.socket().close();
				}
				catch(final IOException ie)
				{
					throw new NetworkException("Error closing socket", ie);
				}
			}
		}
		catch(final IOException ie)
		{
			// (01.09.2012)XXX: not sure if this is reasonable:
			// On exception, remove this channel from the selector
			key.cancel();
		}
	}

	protected boolean processInput(final SocketChannel sc) throws IOException
	{
		this.buffer.clear();
		sc.read(this.buffer);
		this.buffer.flip();

		// If no data, close the connection
		if(this.buffer.limit()==0)
		{
			return false;
		}

		// processing logic

		System.out.println( "Processed "+this.buffer.limit()+" from "+sc );

		return true; // (01.09.2012)FIXME: noobish flag has to be replaced by something proper
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public void run()
	{
		try
		{
			while(true)
			{
				// Wait for activity - either an incoming connection, or incoming data on an existing connection
				if(this.selector.select() != 0)
				{
					this.receive();
				}
				else
				{
					Thread.sleep(10);
				}
			}
		}
		catch(final Exception ie)
		{
			// (01.09.2012)FIXME: on-error clean up of resources (actually only here?)
			throw new NetworkException(ie);
		}
	}



	static public void main(final String args[]) throws Throwable
	{
		start(new NetworkChannelSampleServer(1337).initialize());
	}



	// nested class for compactness of sample. Would normally get its own compilation unit.
	public static class NetworkException extends RuntimeException
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

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

		public NetworkException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace)
		{
			super(message, cause, enableSuppression, writableStackTrace);
		}



		}

}
