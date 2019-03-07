package one.microstream.network.types;

import static one.microstream.X.notNull;

import java.nio.channels.SocketChannel;
import java.util.function.Consumer;

import one.microstream.functional.ParallelProcedure;

public interface NetworkConnectionProcessor extends Consumer<SocketChannel>
{
	@Override
	public void accept(SocketChannel connection);



	public interface Provider<P extends NetworkConnectionProcessor>
	extends ParallelProcedure.LogicProvider<SocketChannel, P>
	{
		@Override
		public P provideLogic();

		@Override // generics 4tw! :D
		public void disposeLogic(P processor, Throwable cause);



		public class TrivialImplementation implements NetworkConnectionProcessor.Provider<NetworkConnectionProcessor>
		{
			private final NetworkConnectionProcessor connectionProcessor;

			public TrivialImplementation(final NetworkConnectionProcessor connectionProcessor)
			{
				super();
				this.connectionProcessor = notNull(connectionProcessor);
			}

			@Override
			public NetworkConnectionProcessor provideLogic()
			{
				return this.connectionProcessor;
			}

			@Override
			public void disposeLogic(final NetworkConnectionProcessor processor, final Throwable cause)
			{
				// no-op
			}

		}

	}

	public interface RegulatorThreadCount extends ParallelProcedure.ThreadCountProvider
	{
		@Override
		public int maxThreadCount();
	}

	public interface RegulatorThreadTimeout extends ParallelProcedure.ThreadTimeoutProvider
	{
		@Override
		public int threadTimeout();
	}

}
