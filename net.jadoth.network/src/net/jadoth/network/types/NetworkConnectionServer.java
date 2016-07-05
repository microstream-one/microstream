package net.jadoth.network.types;

import static net.jadoth.Jadoth.notNull;
import static net.jadoth.math.JadothMath.positive;


public interface NetworkConnectionServer extends NetworkServer
{
	public int getConnectionListenerThreadCount();
	public int getConnectionListenerCheckInterval();
	public int getConnectionProcessorThreadCount();
	public int getConnectionProcessorThreadTimeout();

	public NetworkConnectionServer setConnectionListenerThreadCount(int maxThreadCount);
	public NetworkConnectionServer setConnectionListenerCheckInterval(int checkInterval);
	public NetworkConnectionServer setConnectionProcessorThreadCount(int maxThreadCount);
	public NetworkConnectionServer setConnectionProcessorThreadTimeout(int timeout);



	public class Implementation implements NetworkConnectionServer
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final NetworkConnectionManager connectionManager ;

		private volatile boolean active             ; // state indicating being completely active
		private volatile boolean shutdown     = true; // state indicating being completely inactive
		private volatile boolean activating         ; // state indicating transition to active state
		private volatile boolean deactivating       ; // state indicating transition to inactive state

		private volatile int valueConLisThreadCount;
		private volatile int valueConLisCheckIntrvl;
		private volatile int valueConPrcThreadCount;
		private volatile int valueConPrcThrdTimeout;

		private final RegulatorConnectionListenerThreadCount    regulatorConLisThreadCount;
		private final RegulatorConnectionListenerCheckInterval  regulatorConLisCheckIntrvl;
		private final RegulatorConnectionProcessorsThreadCount  regulatorConPrcThreadCount;
		private final RegulatorConnectionProcessorThreadTimeout regulatorConPrcThrdTimeout;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(final Setup setup)
		{
			super();
			this.connectionManager          =  notNull(setup.connectionManager                          );
			this.valueConLisThreadCount     = positive(setup.regulatorConLisThreadCount.maxThreadCount());
			this.valueConLisCheckIntrvl     = positive(setup.regulatorConLisCheckIntrvl.checkInterval() );
			this.valueConPrcThreadCount     = positive(setup.regulatorConPrcThreadCount.maxThreadCount());
			this.valueConPrcThrdTimeout     = positive(setup.regulatorConPrcThrdTimeout.threadTimeout() );
			this.regulatorConLisThreadCount =  notNull(setup.regulatorConLisThreadCount                 );
			this.regulatorConLisCheckIntrvl =  notNull(setup.regulatorConLisCheckIntrvl                 );
			this.regulatorConPrcThreadCount =  notNull(setup.regulatorConPrcThreadCount                 );
			this.regulatorConPrcThrdTimeout =  notNull(setup.regulatorConPrcThrdTimeout                 );
		}



		///////////////////////////////////////////////////////////////////////////
		// getters          //
		/////////////////////

		@Override
		public final synchronized int getConnectionListenerThreadCount()
		{
			return this.valueConLisThreadCount;
		}

		@Override
		public final int getConnectionListenerCheckInterval()
		{
			return this.valueConLisCheckIntrvl;
		}

		@Override
		public final synchronized int getConnectionProcessorThreadCount()
		{
			return this.valueConPrcThreadCount;
		}

		@Override
		public final synchronized int getConnectionProcessorThreadTimeout()
		{
			return this.valueConPrcThrdTimeout;
		}



		///////////////////////////////////////////////////////////////////////////
		// setters          //
		/////////////////////

		protected final void internalSetConnectionListenerThreadCount(final int maxThreadCount)
		{
			this.valueConLisThreadCount = positive(maxThreadCount);
			if(this.active)
			{
				this.regulatorConLisThreadCount.setMaxThreadCount(maxThreadCount);
			}
		}

		protected final void internalSetConnectionListenerCheckInterval(final int checkInterval)
		{
			this.valueConLisCheckIntrvl = positive(checkInterval);
			if(this.active)
			{
				this.regulatorConLisCheckIntrvl.setCheckInterval(checkInterval);
			}
		}

		protected final void internalSetConnectionProcessorThreadCount(final int maxThreadCount)
		{
			this.valueConPrcThreadCount = positive(maxThreadCount);
			if(this.active)
			{
				this.regulatorConPrcThreadCount.setMaxThreadCount(maxThreadCount);
			}
		}

		protected final void internalSetConnectionProcessorThreadTimeout(final int timeout)
		{
			this.valueConPrcThrdTimeout = positive(timeout);
			if(this.active)
			{
				this.regulatorConPrcThrdTimeout.setTimeout(timeout);
			}
		}

		@Override
		public synchronized NetworkConnectionServer setConnectionListenerThreadCount(final int maxThreadCount)
		{
			this.internalSetConnectionListenerThreadCount(maxThreadCount);
			return this;
		}

		@Override
		public NetworkConnectionServer setConnectionListenerCheckInterval(final int checkInterval)
		{
			this.internalSetConnectionListenerCheckInterval(checkInterval);
			return this;
		}

		@Override
		public synchronized NetworkConnectionServer setConnectionProcessorThreadCount(final int maxThreadCount)
		{
			this.internalSetConnectionProcessorThreadCount(maxThreadCount);
			return this;
		}

		@Override
		public synchronized NetworkConnectionServer setConnectionProcessorThreadTimeout(final int timeout)
		{
			this.internalSetConnectionProcessorThreadTimeout(timeout);
			return this;
		}

		protected void internalActivate()
		{
			this.regulatorConLisThreadCount.setMaxThreadCount(this.valueConLisThreadCount);
			this.regulatorConLisCheckIntrvl.setCheckInterval (this.valueConLisCheckIntrvl);
			this.regulatorConPrcThreadCount.setMaxThreadCount(this.valueConPrcThreadCount);
			this.regulatorConPrcThrdTimeout.setTimeout       (this.valueConPrcThrdTimeout);
			this.connectionManager.activate();
		}

		@Override
		public synchronized boolean activate()
		{
			if(!this.shutdown)
			{
				return false;
			}
			this.activating = true;
			this.shutdown = false;
			this.internalActivate();
			this.active = true;
			this.activating = false;
			return true;
		}

		@Override
		public final boolean isActive()
		{
			return this.active;
		}

		@Override
		public final boolean isActivating()
		{
			return this.activating;
		}

		@Override
		public final boolean isDeactivating()
		{
			return this.deactivating;
		}

		@Override
		public final boolean isShutdown()
		{
			return this.shutdown;
		}

		protected void internalDeactivate()
		{
			this.regulatorConLisThreadCount.setMaxThreadCount(0);
			this.regulatorConLisCheckIntrvl.setCheckInterval (1);
			this.regulatorConPrcThreadCount.setMaxThreadCount(0);
			this.regulatorConPrcThrdTimeout.setTimeout       (1);
			this.connectionManager.deactivate();
		}

		@Override
		public synchronized boolean deactivate()
		{
			if(!this.active)
			{
				return false;
			}
			this.deactivating = true;
			this.active = false;
			this.internalDeactivate();
			// (01.10.2012)FIXME: wait for all threads to die ? How to bring in threads from subclasses?
			this.shutdown = true;
			this.deactivating = false;
			return true;
		}



		public static final class RegulatorConnectionListenerThreadCount
		implements NetworkConnectionListener.RegulatorThreadCount
		{
			static final int DEFAULT_THREAD_COUNT = 1;

			private int threadCount = DEFAULT_THREAD_COUNT;

			protected void setMaxThreadCount(final int maxThreadCount)
			{
				this.threadCount = maxThreadCount;
			}

			@Override
			public int maxThreadCount()
			{
				// should be safe without volatile because listener threads synchronize all over the place anyway
				return this.threadCount;
			}

		}

		public static final class RegulatorConnectionProcessorsThreadCount
		implements NetworkConnectionProcessor.RegulatorThreadCount
		{
			static final int DEFAULT_THREAD_COUNT = 1;

			private int threadCount = DEFAULT_THREAD_COUNT;

			void setMaxThreadCount(final int maxThreadCount)
			{
				this.threadCount = maxThreadCount;
			}

			@Override
			public int maxThreadCount()
			{
				// should be safe without volatile because listener threads synchronize all over the place anyway
				return this.threadCount;
			}

		}

		public static final class RegulatorConnectionProcessorThreadTimeout
		implements NetworkConnectionProcessor.RegulatorThreadTimeout
		{
			static final int DEFAULT_TIMEOUT = 1000;

			private int threadTimeout = DEFAULT_TIMEOUT;

			void setTimeout(final int threadTimeout)
			{
				this.threadTimeout = threadTimeout;
			}

			@Override
			public int threadTimeout()
			{
				return this.threadTimeout;
			}

		}

		public static final class RegulatorConnectionListenerCheckInterval
		implements NetworkConnectionListener.RegulatorCheckInterval
		{
			static final int DEFAULT_INTERVAL = 1000;

			private int checkInterval = DEFAULT_INTERVAL;

			void setCheckInterval(final int checkInterval)
			{
				this.checkInterval = checkInterval;
			}

			@Override
			public int checkInterval()
			{
				return this.checkInterval;
			}

		}

		public static class Setup
		{
			final NetworkConnectionManager                  connectionManager         ;
			final RegulatorConnectionListenerThreadCount    regulatorConLisThreadCount;
			final RegulatorConnectionListenerCheckInterval  regulatorConLisCheckIntrvl;
			final RegulatorConnectionProcessorsThreadCount  regulatorConPrcThreadCount;
			final RegulatorConnectionProcessorThreadTimeout regulatorConPrcThrdTimeout;

			public Setup(
				final NetworkConnectionManager                  connectionManager         ,
				final RegulatorConnectionListenerThreadCount    regulatorConLisThreadCount,
				final RegulatorConnectionListenerCheckInterval  regulatorConLisCheckIntrvl,
				final RegulatorConnectionProcessorsThreadCount  regulatorConPrcThreadCount,
				final RegulatorConnectionProcessorThreadTimeout regulatorConPrcThrdTimeout
			)
			{
				super();
				this.connectionManager          = connectionManager         ;
				this.regulatorConLisThreadCount = regulatorConLisThreadCount;
				this.regulatorConLisCheckIntrvl = regulatorConLisCheckIntrvl;
				this.regulatorConPrcThreadCount = regulatorConPrcThreadCount;
				this.regulatorConPrcThrdTimeout = regulatorConPrcThrdTimeout;
			}

		}

	}

}
