package one.microstream.communication.types;

/*-
 * #%L
 * microstream-communication
 * %%
 * Copyright (C) 2019 - 2021 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;

import one.microstream.chars.XChars;
import one.microstream.chars._charArrayRange;
import one.microstream.com.ComException;
import one.microstream.com.ComExceptionTimeout;
import one.microstream.com.XSockets;
import one.microstream.memory.XMemory;
import one.microstream.util.logging.Logging;

/**
 * 
 *
 * @param <C> the communication layer type
 */
public interface ComConnectionHandler<C>
{
	///////////////////////////////////////////////////////////////////////////
	// interface methods //
	//////////////////////
	
	public ComConnectionListener<C> createConnectionListener(InetSocketAddress address);
	
	public C openConnection(InetSocketAddress address);
	
	public C openConnection(InetSocketAddress hostAddress, int retries, Duration retryDelay);
		
	public void prepareReading(C connection);
	
	public void prepareWriting(C connection);
	
	public void close(C connection);
	
	public void closeReading(C connection);
	
	public void closeWriting(C connection);
		
	public void read(C connction, ByteBuffer buffer);
	
	public void write(C connction, ByteBuffer buffer);
	
	public default void writeChunk(
		final C             connection  ,
		final ByteBuffer    headerBuffer,
		final ByteBuffer[]  buffers
	)
	{
		this.write(connection, headerBuffer);
		
		for(final ByteBuffer buffer : buffers)
		{
			this.write(connection, buffer);
		}
	}
	
	public void sendProtocol(C connection, ComProtocol protocol, ComProtocolStringConverter stringConverter);
	
	public ComProtocol receiveProtocol(C connection, ComProtocolStringConverter stringConverter);
		
	public void setInactivityTimeout(C connection, int inactivityTimeout);
	
	public void setClientConnectTimeout(int clientConnectTimeout);
	
	public void sendClientIdentifer(C connection, ComPeerIdentifier peerIdentifier);
	
	public void receiveClientIdentifer(final C connection, final ByteBuffer buffer);
	
	public void enableSecurity(C connection);
	
	public static ComConnectionHandler.Default Default()
	{
		return new ComConnectionHandler.Default();
	}
	
	public class Default implements ComConnectionHandler<ComConnection>
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////
		
		private final static Logger logger = Logging.getLogger(ComConnectionHandler.class);
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final int protocolLengthDigitCount = Com.defaultProtocolLengthDigitCount();
		private long clientConnectTimeout;
								
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Default()
		{
			super();
		}
		
			
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		
		private ComConnection openConnection(final InetSocketAddress address, final long timeout)
		{
			final ExecutorService executor = Executors.newSingleThreadExecutor();
			
			try
			{
				return executor
					.submit(() -> { return this.openConnection(address); })
					.get(timeout, TimeUnit.MILLISECONDS);
			}
			catch(final TimeoutException e)
			{
				throw new ComExceptionTimeout("Open connection failed because of timeout");
			}
			catch (final Exception e)
			{
				throw new ComException("Open connection failed", e);
			}
			finally
			{
				executor.shutdownNow();
			}
		}
		
		@Override
		public ComConnectionListener<ComConnection> createConnectionListener(
			final InetSocketAddress address
		)
		{
			final ServerSocketChannel serverSocketChannel = XSockets.openServerSocketChannel(address);
			final ComConnectionListener<ComConnection> connectionListener = new ComConnectionListener.Default(serverSocketChannel);
			logger.debug("created new ComConnectionListener {}", connectionListener);
			return connectionListener;
		}
				
		@Override
		public ComConnection openConnection(final InetSocketAddress address)
		{
			final SocketChannel clientChannel = XSockets.openChannel(address);
			final ComConnection connection = new ComConnection.Default(clientChannel);
			logger.debug("created new ComConnection {}", connection);
			return connection;
		}
		
		@Override
		public ComConnection openConnection(final InetSocketAddress address, final int retries, final Duration retryDelay)
		{
			int tries = 0;
			
			do
			{
				try
				{
					tries++;
					if(this.clientConnectTimeout > 0)
					{
						return this.openConnection(address, this.clientConnectTimeout);
					}
					return this.openConnection(address);
				}
				catch(final Exception connectException)
				{
					if(tries <= retries)
					{
						try
						{
							Thread.sleep(retryDelay.toMillis());
						}
						catch (final InterruptedException interruptedException)
						{
							throw new ComException("Connect to " + address + " failed", interruptedException);
						}
					}
					else
					{
						throw new ComException("Connect to " + address + " failed", connectException);
					}
				}
				
				logger.debug("Connection attempt {} of {} failed", tries, retries);
			}
			while(tries <= retries);
			
			//Should not be reached. If a connection can't be opened an exception should have been thrown already
			throw new ComException("Connect to " + address + " failed");
		}

		@Override
		public void prepareReading(final ComConnection connection)
		{
			// no preparation needed for SocketChannel instances
		}

		@Override
		public void prepareWriting(final ComConnection connection)
		{
			// no preparation needed for SocketChannel instances
		}

		@Override
		public void close(final ComConnection connection)
		{
			connection.close();
		}

		@Override
		public void closeReading(final ComConnection connection)
		{
			// (17.11.2018 TM)TODO: SocketChannel#shutdownInput ?
			
			// SocketChannel#close is idempotent
			this.close(connection);
		}

		@Override
		public void closeWriting(final ComConnection connection)
		{
			// (17.11.2018 TM)TODO: SocketChannel#shutdownOutput ?
			
			// SocketChannel#close is idempotent
			this.close(connection);
		}

		@Override
		public void read(final ComConnection connection, final ByteBuffer buffer)
		{
			connection.readCompletely(buffer);
		}

		@Override
		public void write(final ComConnection connection, final ByteBuffer buffer)
		{
			connection.writeCompletely(buffer);
		}
		
		
		@Override
		public void sendClientIdentifer(final ComConnection connection, final ComPeerIdentifier peerIdentifier)
		{
			logger.info("Sending client identifer {} ", peerIdentifier);
			connection.writeUnsecured(peerIdentifier.getBuffer());
		}
		
		@Override
		public void receiveClientIdentifer(final ComConnection connection, final ByteBuffer buffer)
		{
			logger.info("Receiving client identifer");
			connection.readUnsecure(buffer);
		}
		
		@Override
		public void sendProtocol(
			final ComConnection              connection     ,
			final ComProtocol                protocol       ,
			final ComProtocolStringConverter stringConverter
		)
		{
			final ByteBuffer bufferedProtocol = Com.bufferProtocol(
				protocol                     ,
				stringConverter              ,
				this.protocolLengthDigitCount
			);
			
			logger.debug("Sending ComProtocol to peer.");
			this.write(connection, bufferedProtocol);
		}
		
		@Override
		public ComProtocol receiveProtocol(
			final ComConnection              connection     ,
			final ComProtocolStringConverter stringConverter
		)
		{
			logger.debug("Awaiting ComProtocol from peer ...");
			
			final ByteBuffer lengthBuffer = XMemory.allocateDirectNative(this.protocolLengthDigitCount);
			this.read(connection, lengthBuffer);
						
			// buffer position must be reset for the decoder to see the bytes
			lengthBuffer.position(0);
			final String lengthDigits = XChars.standardCharset().decode(lengthBuffer).toString();
			final int    length       = Integer.parseInt(lengthDigits);
			
			final ByteBuffer protocolBuffer = XMemory.allocateDirectNative(length - this.protocolLengthDigitCount);
			this.read(connection, protocolBuffer);
			
			// buffer position must be reset to after the separator for the decoder to see the bytes
			protocolBuffer.position(1);
			final char[] protocolChars = XChars.standardCharset().decode(protocolBuffer).array();
			
			final ComProtocol protocol = stringConverter.parse(_charArrayRange.New(protocolChars));
			
			logger.debug("Received ComProtocol from peer successfully.");
			return protocol;
		}


		@Override
		public void enableSecurity(final ComConnection connection)
		{
			//The default Connection is not encrypted, nothing to do
			logger.warn("Using unsecured connection!");
		}


		@Override
		public void setInactivityTimeout(final ComConnection connection, final int inactivityTimeout)
		{
			connection.setTimeOut(inactivityTimeout);
			logger.debug("Set connection inactivity timeout {}", inactivityTimeout);
		}
		
		@Override
		public void setClientConnectTimeout(final int clientConnectTimeout)
		{
			this.clientConnectTimeout = clientConnectTimeout;
		}
		
	}
	
}
