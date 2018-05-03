package net.jadoth.persistence.types;

import static java.lang.System.identityHashCode;

import net.jadoth.math.JadothMath;
import net.jadoth.util.VMUtils;


public interface ServerConnection
{
	public <T> T request(NetworkRequest<T> request);

	public <T> T request(NetworkRequest<T> request, long timeout);

	// (22.12.2012)TODO means to register additional waiting thread for a request?

	public void send(NetworkRequest<?> request);



	public class Implementation implements ServerConnection
	{
		///////////////////////////////////////////////////////////////////////////
		// constants        //
		/////////////////////

		private static final int HASH_LIMIT = JadothMath.highestPowerOf2Integer();

		// (15.04.2016 TM)NOTE: no idea why this is so static. Old code.
		private static final int RESPONSE_ENTRY_ARRAY_LENGTH = 32;


		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final NetworkResponseHandler responseHandler;

		private ResponseEntry[] waitItemsHashSlots = new ResponseEntry[RESPONSE_ENTRY_ARRAY_LENGTH];
		private int             waitItemsHashRange = this.waitItemsHashSlots.length - 1            ;
		private int             waitItemsSize                                                      ;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(final NetworkResponseHandler responseHandler)
		{
			super();
			this.responseHandler = responseHandler;
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		protected final boolean isAlreadySent(final NetworkRequest<?> request)
		{
			for(ResponseEntry e = this.waitItemsHashSlots[identityHashCode(request) & this.waitItemsHashRange]; e != null; e = e.link)
			{
				if(e.request == request)
				{
					return true;
				}
			}
			return false;
		}

		private final synchronized ResponseEntry registerAndSend(
			final NetworkRequest<?> request      ,
			final Thread            waitingThread
		)
		{
			if(this.isAlreadySent(request))
			{
				throw new RuntimeException("Request already sent"); // (22.12.2012)FIXME: proper exception
			}
			final ResponseEntry entry = this.putEntry(request, waitingThread);
			if(++this.waitItemsSize >= this.waitItemsHashRange)
			{
				this.rebuildBuildItems();
			}
			this.enqueueRequest(request);
			return entry;
		}

		private final ResponseEntry putEntry(final NetworkRequest<?> request, final Thread waitingThread)
		{
			return this.waitItemsHashSlots[identityHashCode(request) & this.waitItemsHashRange] =
				new ResponseEntry(
					request,
					waitingThread,
					this.waitItemsHashSlots[identityHashCode(request) & this.waitItemsHashRange]
				)
			;
		}

		private final void enqueueRequest(final NetworkRequest<?> request)
		{
			// FIXME enqueue request
			throw new net.jadoth.meta.NotImplementedYetError();
		}

		private void rebuildBuildItems()
		{
			// moreless academic check for more than 1 billion entries
			if(this.waitItemsHashSlots.length >= HASH_LIMIT)
			{
				return; // note that aborting rebuild does not ruin anything, only performance degrades
			}

			// potential int overflow ignored deliberately
			final int newRange = (this.waitItemsHashSlots.length << 1) - 1;
			final ResponseEntry[] newSlots = new ResponseEntry[newRange + 1];
			for(ResponseEntry entry : this.waitItemsHashSlots)
			{
				for(ResponseEntry next; entry != null; entry = next)
				{
					next = entry.link;
					entry.link = newSlots[identityHashCode(entry.request) & newRange];
					newSlots[identityHashCode(entry.request) & newRange] = entry;
				}
			}
			this.waitItemsHashSlots = newSlots;
			this.waitItemsHashRange = newRange;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public <T> T request(final NetworkRequest<T> request)
		{
			return this.request(request, 0);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T request(final NetworkRequest<T> request, final long timeout)
		{
			final Object response = this.registerAndSend(request, Thread.currentThread()).waitForResponse(timeout);
			if(response instanceof Throwable)
			{
				VMUtils.throwUnchecked((Throwable)response); // low-level Throwable throwing, no other way.
			}
			return (T)response;
		}

		@Override
		public void send(final NetworkRequest<?> request)
		{
			this.registerAndSend(request, null);
		}

		final void handleResponse(final ResponseEntry entry)
		{
			if(entry.waitingThread == null)
			{
				this.responseHandler.handleResponse(entry.response);
				return;
			}

			synchronized(entry)
			{
				entry.notify(); // logic ensures that at most one thread can wait on an entry
			}
		}



		private static final class ResponseEntry
		{
			final NetworkRequest<?> request      ;
			final Thread            waitingThread;
			      ResponseEntry     link         ;

			volatile Object response;

			ResponseEntry(final NetworkRequest<?> request, final Thread waitingThread, final ResponseEntry link)
			{
				super();
				this.request       = request;
				this.waitingThread = waitingThread;
				this.link          = link;
			}

			final synchronized Object waitForResponse(final long timeout)
			{
				try
				{
					while(this.response == null)
					{
						this.wait(timeout);
					}
				}
				catch(final InterruptedException e)
				{
					throw new RuntimeException("Aborted", e); // (22.12.2012)FIXME: proper exception
				}
				return this.response;
			}

		}

	}

}
