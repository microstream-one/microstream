package one.microstream.communication.tls;

/*-
 * #%L
 * MicroStream Communication Binary
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
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

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

import org.slf4j.Logger;

import one.microstream.com.ComException;
import one.microstream.com.ComExceptionTimeout;
import one.microstream.com.XSockets;
import one.microstream.communication.types.ComConnection;
import one.microstream.util.logging.Logging;

public class ComTLSConnection implements ComConnection
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
		
	private final SocketChannel channel;
	private SSLEngine           sslEngine;
	private ByteBuffer          sslEncyptedOut;
	private ByteBuffer          sslEncryptedIn;
	private ByteBuffer          sslDecrypted;
	
	/**
	 * Timeout for blocking read operations during TLS handshake
	 */
	private       int                   readTimeOut;
	private final SSLContext            sslContext;
	private final boolean               clientMode;
	private final TLSParametersProvider tlsParameterProvider;
	
	private final Logger logger = Logging.getLogger(ComConnection.class);
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ComTLSConnection(
		final SocketChannel         channel             ,
		final SSLContext            sslContext          ,
		final TLSParametersProvider tlsParameterProvider,
		final boolean               clientMode
	)
	{
		super();
		this.readTimeOut          = tlsParameterProvider.getHandshakeReadTimeOut();
		this.channel              = channel;
		this.sslContext           = sslContext;
		this.clientMode           = clientMode;
		this.tlsParameterProvider = tlsParameterProvider;
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public void readCompletely(final ByteBuffer buffer)
	{
		this.read(buffer, buffer.capacity());
	}
	
	@Override
	public void readUnsecure(final ByteBuffer buffer)
	{
		this.readInternal(buffer);
	}
	
	@Override
	public void writeUnsecured(final ByteBuffer buffer)
	{
		XSockets.writeCompletely(this.channel, buffer);
	}
	
	@Override
	public ByteBuffer read(final ByteBuffer defaultBuffer, final int length)
	{
		if(!this.channel.isOpen())
		{
			throw new ComException("Can not read from closed channel!");
		}
		
		final ByteBuffer outBuffer = this.ensureOutBufferSize(defaultBuffer, length);
		
		while(outBuffer.position() < length)
		{
			if(this.sslDecrypted.position() == 0)
			{
				this.decryptPackage();
			}
			else
			{
				this.appendDecrypedData(outBuffer, length);
			}
		}
			
		return outBuffer;
	}
	
	@Override
	public void close()
	{
		if(this.sslEngine != null)
		{
			this.closeSSLEngine();
		}
		
		XSockets.closeChannel(this.channel);
		
		this.logger.debug("closed connection {}", this);
	}
	
	private void closeSSLEngine()
	{
		//this zero sized buffer is needed for the SSLEngine to create the closing messages
		final ByteBuffer emptyBuffer = ByteBuffer.allocate(0);
		SSLEngineResult result;
				
		this.sslEngine.closeOutbound();
		
		while(!this.sslEngine.isOutboundDone())
		{
			this.sslEncyptedOut.clear();
			try
			{
				result = this.sslEngine.wrap(emptyBuffer, this.sslEncyptedOut);
			}
			catch (final SSLException e)
			{
				throw new ComException("failed to encrypt buffer", e);
			}
			if(result.getStatus() == Status.OK)
			{
				XSockets.writeCompletely(this.channel, this.sslEncyptedOut);
			}
			
			this.sslEncyptedOut.compact();
		}
	}

	@Override
	public void writeCompletely(final ByteBuffer buffer)
	{
		this.write(buffer, 0);
	}


	@Override
	public void write(final ByteBuffer buffer, final int timeout)
	{
		if(!this.channel.isOpen())
		{
			throw new ComException("Can not write to closed channel!");
		}
				
		while(buffer.remaining() > 0)
		{
			final SSLEngineResult result;
			
			try
			{
				result = this.sslEngine.wrap(buffer, this.sslEncyptedOut);
				this.sslEncyptedOut.flip();
			}
			catch (final SSLException e)
			{
				throw new ComException("failed to encrypt buffer", e);
			}
			
			switch(result.getStatus())
			{
				case BUFFER_OVERFLOW:
					throw new ComException("Unexpected sslEngine wrap result: " + result.getStatus());
				case BUFFER_UNDERFLOW:
					throw new ComException("Unexpected sslEngine wrap result: " + result.getStatus());
				case CLOSED:
					throw new ComException("Unexpected sslEngine wrap result: " + result.getStatus());
				case OK:
					XSockets.writeCompletely(this.channel, this.sslEncyptedOut);
					break;
				default:
					throw new ComException("Unexpected sslEngine wrap result: " + result.getStatus());
			}
					
			this.sslEncyptedOut.clear();
		}
	}
	
	private void readInternal(final ByteBuffer buffer)
	{
		if(this.readTimeOut > 0)
		{
			 this.readInternalWithTimeout(buffer);
		}
		else
		{
			this.readInternalNoTimeout(buffer);
		}
	}
	
	private void readInternalWithTimeout(final ByteBuffer buffer)
	{
		final ExecutorService executor = Executors.newSingleThreadExecutor();
				
		int readResult = 0;
		
		try
		{
			readResult = executor
				.submit(() -> { return this.channel.read(buffer); })
				.get(this.readTimeOut, TimeUnit.MILLISECONDS);
		}
		catch (InterruptedException | ExecutionException e)
		{
			throw new ComException("reading data failed", e);
		}
		catch(final TimeoutException e)
		{
			throw new ComExceptionTimeout("read timeout after " + this.readTimeOut + " " + TimeUnit.MILLISECONDS, e);
		}
		finally
		{
			executor.shutdownNow();
		}

		if(readResult < 0)
		{
			throw new ComException("reading data during handshake failed");
		}
	}
	
	private HandshakeStatus unwrapHandshakeData() throws IOException
	{
		SSLEngineResult.HandshakeStatus hs = this.sslEngine.getHandshakeStatus();
		
		if(this.sslEncryptedIn.position() == 0)
		{
			this.readInternal(this.sslEncryptedIn);
		}
		
		this.sslEncryptedIn.flip();
		
		while(hs == HandshakeStatus.NEED_UNWRAP &&
			this.sslEncryptedIn.hasRemaining())
		{
			final SSLEngineResult engineResult = this.sslEngine.unwrap(this.sslEncryptedIn, this.sslDecrypted);
			hs = engineResult.getHandshakeStatus();
			
			final Status status = engineResult.getStatus();
			
			if(status != Status.OK)
			{
				if(status == Status.CLOSED || status == Status.BUFFER_OVERFLOW)
				{
					throw new ComException("TLS Handshake failed with engine status " + status);
				}
				
				if(status == Status.BUFFER_UNDERFLOW)
				{
					this.readInternal(this.sslEncryptedIn);
				}
			}
		}
		
		this.sslEncryptedIn.compact();
		
		return hs;
	}
	
	private HandshakeStatus wrapHandshakeData(final ByteBuffer handshakeData) throws IOException
	{
		this.sslEncyptedOut.clear();
		final SSLEngineResult engineResult = this.sslEngine.wrap(handshakeData, this.sslEncyptedOut);
		final SSLEngineResult.HandshakeStatus hs = engineResult.getHandshakeStatus();
											
		if(engineResult.getStatus() == SSLEngineResult.Status.OK )
		{
			this.sslEncyptedOut.flip();
			this.channel.write(this.sslEncyptedOut);
			this.sslEncyptedOut.compact();
		}
		
		return hs;
	}
	
	private HandshakeStatus executeHandshakeTask()
	{
		final Runnable task = this.sslEngine.getDelegatedTask();
		if(task != null)
		{
			final Thread engineTask = new Thread(task);
			engineTask.start();
			try
			{
				engineTask.join();
			}
			catch (final InterruptedException e)
			{
				throw new ComException("Error in SSLEngine handshake task ", e);
			}
		}
		
		return this.sslEngine.getHandshakeStatus();
	}
	
	private void doHandshake() throws IOException
	{
		final SSLSession session = this.sslEngine.getSession();
		final ByteBuffer handshakeData = ByteBuffer.allocate(session.getApplicationBufferSize());
		
		this.sslEngine.beginHandshake();
		SSLEngineResult.HandshakeStatus hs = this.sslEngine.getHandshakeStatus();
		
		while (hs != SSLEngineResult.HandshakeStatus.FINISHED &&
			hs != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING)
		{
			switch (hs)
			{
				case NEED_UNWRAP:
					hs = this.unwrapHandshakeData();
					break;
					
				case NEED_WRAP :
					hs = this.wrapHandshakeData(handshakeData);
					break;
					
				case NEED_TASK :
					hs = this.executeHandshakeTask();
					break;
					
				default:
					//should never happen but if so throw an exception to avoid unknown behavior during the SSL handshake
					throw new ComException("Unexpected handshake status: " + hs );
			}
		}
	}
		
	/**
	 * read network data and decrypt until one block is done
	 */
	private void decryptPackage()
	{
		boolean needMoreData = true;
		if(this.sslEncryptedIn.position() > 0)
		{
			this.sslEncryptedIn.flip();
			final SSLEngineResult result = this.unwrapData();
						
			if(result.getStatus() == Status.OK)
			{
				needMoreData = false;
				this.sslEncryptedIn.compact();
			}
			
			if(result.getStatus() == Status.BUFFER_UNDERFLOW)
			{
				this.sslEncryptedIn.position(this.sslEncryptedIn.limit());
				this.sslEncryptedIn.limit(this.sslEncryptedIn.capacity());
			}
			
			if(result.getStatus() == Status.CLOSED)
			{
				this.close();
			}
		}
							
		if(needMoreData)
		{
			this.readInternal(this.sslEncryptedIn);
		}
	}


	/**
	 * Append already decrypted data to the supplied buffer
	 * 
	 * @param outBuffer
	 * @param length
	 */
	private void appendDecrypedData(final ByteBuffer outBuffer, final int length)
	{
		this.sslDecrypted.flip();
		final int numBytes = Math.min(length, this.sslDecrypted.limit());
		
		try
		{
			outBuffer.put(this.sslDecrypted.array(), 0, numBytes);
		}
		catch(final IndexOutOfBoundsException | BufferOverflowException e)
		{
			throw new ComException("faild to copy to out buffer", e);
		}
		
		this.sslDecrypted.position(numBytes);
		this.sslDecrypted.compact();
		
	}

	/**
	 *
	 * Read from the channel into buffer
	 * throws a ComException if the channel reached the end of stream
	 * This method blocks without a timeout
	 *
	 * @param buffer
	 */
	private void readInternalNoTimeout(final ByteBuffer buffer)
	{
		final int bytesRead;

		try
		{
			bytesRead = this.channel.read(buffer);
		}
		catch (final IOException e)
		{
			throw new ComException("failed reading from channel", e);
		}

		if(bytesRead < 0)
		{
			throw new ComException("reached end of stream unexpected");
		}
	}
	
	/**
	 * If the supplied buffer is to small to hold the required input size
	 * a new appropriate buffer is created;
	 * @param length
	 * @param defaultBuffer
	 * 
	 * @return ByteBuffer
	 */
	private ByteBuffer ensureOutBufferSize(final ByteBuffer defaultBuffer, final int length)
	{
		final ByteBuffer outBuffer;
		
		if(defaultBuffer.capacity() < length)
		{
			outBuffer = ByteBuffer.allocateDirect(length);
		}
		else
		{
			outBuffer = defaultBuffer;
			outBuffer.clear();
		}
		
		return outBuffer;
	}
	
	/**
	 * Unwrap data from the encrypted input buffer into the decrypted data buffer
	 * 
	 * @return SSLEngineResult
	 */
	private SSLEngineResult unwrapData()
	{
		try
		{
			return this.sslEngine.unwrap(this.sslEncryptedIn, this.sslDecrypted);
		}
		catch (final SSLException e)
		{
			throw new ComException("failed to decrypt buffer", e);
		}
	}

	@Override
	public void enableSecurity()
	{
		this.sslEngine = this.sslContext.createSSLEngine();
		this.sslEngine.setUseClientMode(this.clientMode);
		this.sslEngine.setSSLParameters(this.tlsParameterProvider.getSSLParameters());
								
		this.sslEncryptedIn = ByteBuffer.allocate(this.sslEngine.getSession().getPacketBufferSize());
		this.sslEncyptedOut = ByteBuffer.allocate(this.sslEngine.getSession().getPacketBufferSize());
		this.sslDecrypted   = ByteBuffer.allocate(this.sslEngine.getSession().getPacketBufferSize());
		
		try
		{
			this.doHandshake();
		}
		catch (final IOException e)
		{
			throw new ComException("TLS handshake failed ", e);
		}
		
		this.logger.debug("SSLSession established {} {}",
			this.sslEngine.getSession().getProtocol(),
			this.sslEngine.getSession().getCipherSuite()
			);
		
	}


	@Override
	public void setTimeOut(final int inactivityTimeout)
	{
		this.readTimeOut = inactivityTimeout;
	}
}
