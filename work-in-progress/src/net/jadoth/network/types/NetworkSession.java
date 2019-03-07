package net.jadoth.network.types;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import net.jadoth.exceptions.IORuntimeException;
import net.jadoth.network.exceptions.NetworkExceptionConnectionValidation;

public interface NetworkSession<M>
{
	public SocketChannel channel();

	public void close();

	public boolean isClosed();

	public boolean isTimedOut(long timeoutInterval);

	public boolean needsProcessing() throws IORuntimeException;

	public M readMessage();



	public interface Creator<S extends NetworkSession<?>>
	{
		public S createSession(SocketChannel connection);
	}

	public interface Provider<S extends NetworkSession<?>>
	{
		public S provideSession(SocketChannel connection) throws NetworkExceptionConnectionValidation;
	}



	public abstract class AbstractImplementation<M> implements NetworkSession<M>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private volatile long    lastTouched   = System.currentTimeMillis();
		private volatile Thread  readingThread;
		private volatile boolean closed       ;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		protected AbstractImplementation()
		{
			super();
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		protected final void touch()
		{
			this.lastTouched = System.currentTimeMillis();
		}

		protected abstract M internalReadMessage();

		protected abstract boolean internalNeedsProcessing() throws IORuntimeException;



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////



		@Override
		public synchronized void close()
		{
			if(this.closed)
			{
				return;
			}
			try
			{
				this.channel().close();
				this.closed = true;
			}
			catch(final IOException e)
			{
				// (06.10.2012)FIXME: close session / handle exception properly. Exceptionhandler?
				e.printStackTrace();
			}
		}

		@Override
		public final boolean isTimedOut(final long timeoutInterval) throws IORuntimeException
		{
			// this method is intentionally not synchronized as all parts of it are naturally thread save

			// interval is expected to be at least 1000 (one second), so timestamp accuracy is not an issue here.
			if(System.currentTimeMillis() - this.lastTouched < timeoutInterval)
			{
				return false;
			}

			// immediately return false if currently busy. Check for pending data not needed or wanted in timeout thread
			if(this.readingThread != null)
			{
				this.touch();
				return false;
			}

			// overdue and not busy, so session is really timed out
			return true;
		}

		@Override
		public final boolean isClosed()
		{
			return this.closed;
		}

		@Override
		public final boolean needsProcessing() throws IORuntimeException
		{
			if(this.readingThread != null)
			{
				this.touch(); // touch to keep session marked as active
				return false; // needs no processing if reading is already in progress, no matter what.
			}

			return this.internalNeedsProcessing();
		}

		@Override
		public synchronized M readMessage()
		{
			this.readingThread = Thread.currentThread();
			this.touch();
			try
			{
				return this.internalReadMessage();
			}
			finally
			{
				this.readingThread = null;
			}
		}

	}

}
