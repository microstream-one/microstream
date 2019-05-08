package one.microstream.network.types;

import static one.microstream.X.notNull;
import static one.microstream.math.XMath.positive;

public interface NetworkSessionServer extends NetworkConnectionServer
{
	@Override
	public boolean activate();

	public int getMessageListenerThreadCount();
	public int getMessageListenerCheckInterval();
	public int getMessageProcessorThreadCount();
	public int getMessageProcessorThreadTimeout();
	public int getSessionTimeout();
	public int getSessionCheckInterval();

	public NetworkConnectionServer setMessageListenerThreadCount(int maxThreadCount);
	public NetworkConnectionServer setMessageListenerCheckInterval(int interval);
	public NetworkConnectionServer setMessageProcessorThreadCount(int maxThreadCount);
	public NetworkConnectionServer setMessageProcessorThreadTimeout(int timeout);
	public NetworkConnectionServer setSessionTimeout(int timeout);
	public NetworkConnectionServer setSessionCheckInterval(int checkInterval);



	public class Default extends NetworkConnectionServer.Default implements NetworkSessionServer
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final NetworkSessionManager<?> sessionManager;

		private volatile int valueMsgLisThreadCount;
		private volatile int valueMsgLisCheckIntrvl;
		private volatile int valueMsgPrcThreadCount;
		private volatile int valueMsgPrcThrdTimeout;
		private volatile int valueSessionTimeout   ;
		private volatile int valueSessionCheckIntvl;

		private final RegulatorMessageListenerThreadCount    regulatorMsgLisThreadCount;
		private final RegulatorMessageListenerCheckInterval  regulatorMsgLisCheckIntrvl;
		private final RegulatorMessageProcessorThreadCount   regulatorMsgPrcThreadCount;
		private final RegulatorMessageProcessorThreadTimeout regulatorMsgPrcThrdTimeout;
		private final RegulatorSessionTimeout                regulatorSessionTimeout   ;
		private final RegulatorSessionCheckInterval          regulatorSessionCheckIntvl;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Default(final Setup setup)
		{
			super(setup.superSetup);
			this.sessionManager             =  notNull(setup.sessionManager                             );
			this.valueMsgLisThreadCount     = positive(setup.regulatorMsgLisThreadCount.maxThreadCount());
			this.valueMsgLisCheckIntrvl     = positive(setup.regulatorMsgLisCheckIntrvl.checkInterval() );
			this.valueMsgPrcThreadCount     = positive(setup.regulatorMsgPrcThreadCount.maxThreadCount());
			this.valueMsgPrcThrdTimeout     = positive(setup.regulatorMsgPrcThrdTimeout.threadTimeout() );
			this.valueSessionTimeout        = positive(setup.regulatorSessionTimeout   .sessionTimeout());
			this.valueSessionCheckIntvl     = positive(setup.regulatorSessionCheckIntvl.checkInterval() );
			this.regulatorMsgLisThreadCount =  notNull(setup.regulatorMsgLisThreadCount                 );
			this.regulatorMsgLisCheckIntrvl =  notNull(setup.regulatorMsgLisCheckIntrvl                 );
			this.regulatorMsgPrcThreadCount =  notNull(setup.regulatorMsgPrcThreadCount                 );
			this.regulatorMsgPrcThrdTimeout =  notNull(setup.regulatorMsgPrcThrdTimeout                 );
			this.regulatorSessionTimeout    =  notNull(setup.regulatorSessionTimeout                    );
			this.regulatorSessionCheckIntvl =  notNull(setup.regulatorSessionCheckIntvl                 );
		}



		///////////////////////////////////////////////////////////////////////////
		// getters //
		////////////

		@Override
		public int getMessageListenerThreadCount()
		{
			return this.valueMsgLisThreadCount;
		}

		@Override
		public int getMessageListenerCheckInterval()
		{
			return this.valueMsgLisCheckIntrvl;
		}

		@Override
		public int getMessageProcessorThreadCount()
		{
			return this.valueMsgPrcThreadCount;
		}

		@Override
		public int getMessageProcessorThreadTimeout()
		{
			return this.valueMsgPrcThrdTimeout;
		}

		@Override
		public int getSessionTimeout()
		{
			return this.valueSessionTimeout;
		}

		@Override
		public int getSessionCheckInterval()
		{
			return this.valueSessionCheckIntvl;
		}



		///////////////////////////////////////////////////////////////////////////
		// setters //
		////////////

		protected final void internalSetMessageListenerThreadCount(final int maxThreadCount)
		{
			this.valueMsgLisThreadCount = positive(maxThreadCount);
			if(this.isActive())
			{
				this.regulatorMsgLisThreadCount.setMaxThreadCount(maxThreadCount);
			}
		}

		protected final void internalSetMessageListenerCheckInterval(final int interval)
		{
			this.valueMsgLisCheckIntrvl = positive(interval);
			if(this.isActive())
			{
				this.regulatorMsgLisCheckIntrvl.setCheckInterval(interval);
			}
		}

		protected final void internalSetMessageProcessorThreadCount(final int maxThreadCount)
		{
			this.valueMsgPrcThreadCount = positive(maxThreadCount);
			if(this.isActive())
			{
				this.regulatorMsgPrcThreadCount.setMaxThreadCount(maxThreadCount);
			}
		}

		protected final void internalSetMessageProcessorThreadTimeout(final int timeout)
		{
			this.valueMsgPrcThrdTimeout = positive(timeout);
			if(this.isActive())
			{
				this.regulatorMsgPrcThrdTimeout.setTimeout(timeout);
			}
		}

		protected final void internalSetSessionTimeout(final int timeout)
		{
			this.valueSessionTimeout = positive(timeout);
			if(this.isActive())
			{
				this.regulatorSessionTimeout.setTimeout(timeout);
			}
		}

		protected final void internalSetSessionCheckInterval(final int checkInterval)
		{
			this.valueSessionCheckIntvl = positive(checkInterval);
			if(this.isActive())
			{
				this.regulatorSessionCheckIntvl.setCheckInterval(checkInterval);
			}
		}

		@Override
		public NetworkConnectionServer setMessageListenerThreadCount(
			final int maxThreadCount
		)
		{
			this.internalSetMessageListenerThreadCount(maxThreadCount);
			return this;
		}

		@Override
		public NetworkConnectionServer setMessageListenerCheckInterval(
			final int interval
		)
		{
			this.internalSetMessageListenerCheckInterval(interval);
			return this;
		}

		@Override
		public synchronized NetworkSessionServer.Default setMessageProcessorThreadCount(
			final int maxThreadCount
		)
		{
			this.internalSetMessageProcessorThreadCount(maxThreadCount);
			return this;
		}

		@Override
		public synchronized NetworkSessionServer.Default setMessageProcessorThreadTimeout(
			final int timeout
		)
		{
			this.internalSetMessageProcessorThreadTimeout(timeout);
			return this;
		}

		@Override
		public synchronized NetworkSessionServer.Default setConnectionListenerThreadCount(
			final int maxThreadCount
		)
		{
			this.internalSetConnectionListenerThreadCount(maxThreadCount);
			return this;
		}

		@Override
		public synchronized NetworkSessionServer.Default setConnectionListenerCheckInterval(
			final int checkInterval
		)
		{
			this.internalSetConnectionListenerCheckInterval(checkInterval);
			return this;
		}

		@Override
		public synchronized NetworkSessionServer.Default setConnectionProcessorThreadCount(
			final int maxThreadCount
		)
		{
			this.internalSetConnectionProcessorThreadCount(maxThreadCount);
			return this;
		}

		@Override
		public synchronized NetworkSessionServer.Default setConnectionProcessorThreadTimeout(
			final int timeout
		)
		{
			this.internalSetConnectionProcessorThreadTimeout(timeout);
			return this;
		}

		@Override
		public NetworkConnectionServer.Default setSessionTimeout(
			final int timeout
		)
		{
			this.internalSetSessionTimeout(timeout);
			return this;
		}

		@Override
		public NetworkConnectionServer.Default setSessionCheckInterval(
			final int checkInterval
		)
		{
			this.internalSetSessionCheckInterval(checkInterval);
			return this;
		}

		@Override
		protected void internalActivate()
		{
			super.internalActivate();
			this.sessionManager.activate();
		}

		@Override
		protected void internalDeactivate()
		{
			this.sessionManager.deactivate();
			super.internalDeactivate();
		}



		public static final class RegulatorMessageProcessorThreadCount
		implements NetworkMessageProcessor.RegulatorThreadCount
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

		public static final class RegulatorMessageProcessorThreadTimeout
		implements NetworkMessageProcessor.RegulatorThreadTimeout
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

		public static final class RegulatorSessionTimeout
		implements NetworkSessionManager.RegulatorSessionTimeout
		{
			static final int DEFAULT_TIMEOUT = 10_000; // 10 second default session timeout

			private int sessionTimeout = DEFAULT_TIMEOUT;

			void setTimeout(final int sessionTimeout)
			{
				this.sessionTimeout = sessionTimeout;
			}

			@Override
			public int sessionTimeout()
			{
				return this.sessionTimeout;
			}

		}

		public static final class RegulatorSessionCheckInterval
		implements NetworkSessionManager.RegulatorSessionCheckInterval
		{
			static final int DEFAULT_INTERVAL = 1000; // ms

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

		public static final class RegulatorMessageListenerThreadCount
		implements NetworkMessageListener.RegulatorThreadCount
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

		public static final class RegulatorMessageListenerCheckInterval
		implements NetworkMessageListener.RegulatorCheckInterval
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
			final NetworkConnectionServer.Default.Setup superSetup                ;
			final NetworkSessionManager<?>                     sessionManager            ;
			final RegulatorMessageListenerThreadCount          regulatorMsgLisThreadCount;
			final RegulatorMessageListenerCheckInterval        regulatorMsgLisCheckIntrvl;
			final RegulatorMessageProcessorThreadCount         regulatorMsgPrcThreadCount;
			final RegulatorMessageProcessorThreadTimeout       regulatorMsgPrcThrdTimeout;
			final RegulatorSessionCheckInterval                regulatorSessionCheckIntvl;
			final RegulatorSessionTimeout                      regulatorSessionTimeout   ;

			public Setup(
				final NetworkConnectionServer.Default.Setup superSetup                ,
				final NetworkSessionManager<?>                     sessionManager            ,
				final RegulatorMessageListenerThreadCount          regulatorMsgLisThreadCount,
				final RegulatorMessageListenerCheckInterval        regulatorMsgLisCheckIntrvl,
				final RegulatorMessageProcessorThreadCount         regulatorMsgPrcThreadCount,
				final RegulatorMessageProcessorThreadTimeout       regulatorMsgPrcThrdTimeout,
				final RegulatorSessionCheckInterval                regulatorSessionCheckIntvl,
				final RegulatorSessionTimeout                      regulatorSessionTimeout
			)
			{
				super();
				this.superSetup                 = superSetup                ;
				this.sessionManager             = sessionManager            ;
				this.regulatorMsgLisThreadCount = regulatorMsgLisThreadCount;
				this.regulatorMsgLisCheckIntrvl = regulatorMsgLisCheckIntrvl;
				this.regulatorMsgPrcThreadCount = regulatorMsgPrcThreadCount;
				this.regulatorMsgPrcThrdTimeout = regulatorMsgPrcThrdTimeout;
				this.regulatorSessionCheckIntvl = regulatorSessionCheckIntvl;
				this.regulatorSessionTimeout    = regulatorSessionTimeout   ;

			}

		}

	}

}
