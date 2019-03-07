package net.jadoth.network.types;

import java.nio.channels.SocketChannel;

import net.jadoth.functional.ParallelProcedure;

/**
 * Type for handling a newly established connection, where handling can be for example
 * processing the complete communication until it is terminated in the same thread that accepted
 * the connection or only creating and registering a new / finding an existing session for the connection
 * or even simply delegating any further work to another thread and returning.
 *
 * @author Thomas Muenz
 */
public interface NetworkConnectionHandler
{
	public void handleConnection(SocketChannel newConnection);



	public interface Provider
	{
		public NetworkConnectionHandler provideConnectionHandler(
			NetworkConnectionProcessor.Provider<?>            processorCreator     ,
			NetworkConnectionProcessor.RegulatorThreadCount   threadCountController,
			NetworkConnectionProcessor.RegulatorThreadTimeout threadTimeoutController
		);

		public void disposeConnectionHandler(NetworkConnectionHandler connectionHandler, Throwable cause);



		public class Implementation implements NetworkConnectionHandler.Provider
		{
			///////////////////////////////////////////////////////////////////////////
			// override methods //
			/////////////////////

			@Override
			public NetworkConnectionHandler provideConnectionHandler(
				final NetworkConnectionProcessor.Provider<?>            connectionProcessorProvider,
				final NetworkConnectionProcessor.RegulatorThreadCount   threadCountController,
				final NetworkConnectionProcessor.RegulatorThreadTimeout threadTimeoutController
			)
			{
				return new NetworkConnectionHandler.Implementation(
					connectionProcessorProvider,
					threadCountController      ,
					threadTimeoutController
				);
			}

			@Override
			public void disposeConnectionHandler(
				final NetworkConnectionHandler connectionHandler,
				final Throwable                cause
			)
			{
				// no-op
			}

		}

	}



	public class Implementation implements NetworkConnectionHandler
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final ParallelProcedure<SocketChannel> threadManager;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(
			final NetworkConnectionProcessor.Provider<?>            connectionProcessorProvider,
			final NetworkConnectionProcessor.RegulatorThreadCount   threadCountController      ,
			final NetworkConnectionProcessor.RegulatorThreadTimeout threadTimeoutController
		)
		{
			super();
			this.threadManager = new ParallelProcedure.Implementation<>(
				connectionProcessorProvider,
				threadCountController      ,
				threadTimeoutController
			);
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		@Override
		public void handleConnection(final SocketChannel newConnection)
		{
			/* (29.09.2012)XXX: what about counting enqueued connections to throttle down heavy load?
			 * Or is there a more elegant way like some kind of adaptive sleep time or so?
			 */
			this.threadManager.accept(newConnection);
		}

	}

}
