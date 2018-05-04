package net.jadoth.network.types;

import net.jadoth.functional.ParallelProcedure;



public interface NetworkMessageHandler<S extends NetworkSession<?>>
{
	public void handleMessage(S messageSession);



	public interface Provider<S extends NetworkSession<?>>
	{
		public NetworkMessageHandler<S> provideMessageHandler(
			NetworkMessageProcessor.Provider<S, ?>         messageProcessorProvider,
			NetworkMessageProcessor.RegulatorThreadCount   threadCountController   ,
			NetworkMessageProcessor.RegulatorThreadTimeout threadTimeoutProvider
		);

		public void disposeMessageHandler(NetworkMessageHandler<S> messageHandler, Throwable cause);


		public class Implementation<S extends NetworkSession<?>> implements NetworkMessageHandler.Provider<S>
		{

			@Override
			public NetworkMessageHandler<S> provideMessageHandler(
				final NetworkMessageProcessor.Provider<S, ?>         messageProcessorProvider,
				final NetworkMessageProcessor.RegulatorThreadCount   threadCountController   ,
				final NetworkMessageProcessor.RegulatorThreadTimeout threadTimeoutProvider
			)
			{
				return new NetworkMessageHandler.Implementation<>(
					messageProcessorProvider,
					threadCountController   ,
					threadTimeoutProvider
				);
			}

			@Override
			public void disposeMessageHandler(final NetworkMessageHandler<S> messageHandler, final Throwable cause)
			{
				// no-op
			}

		}

	}


	public class Implementation<S extends NetworkSession<?>> implements NetworkMessageHandler<S>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final ParallelProcedure<S> threadManager;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(
			final NetworkMessageProcessor.Provider<S, ?>          messageProcessorProvider,
			final NetworkMessageProcessor.RegulatorThreadCount   threadCountController   ,
			final NetworkMessageProcessor.RegulatorThreadTimeout threadTimeoutProvider
		)
		{
			super();
			this.threadManager = new ParallelProcedure.Implementation<>(
				messageProcessorProvider,
				threadCountController,
				threadTimeoutProvider
			);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public void handleMessage(final S messageSession)
		{
			/* (24.10.2012)XXX: what about counting enqueued messageSessions to throttle down heavy load?
			 * Or is there a more elegant way like some kind of adaptive sleep time or so?
			 */
			this.threadManager.accept(messageSession);
		}

	}

}
