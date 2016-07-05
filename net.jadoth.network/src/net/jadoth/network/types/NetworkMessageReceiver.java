package net.jadoth.network.types;

import static net.jadoth.Jadoth.notNull;

public interface NetworkMessageReceiver<S extends NetworkSession<?>>
{
	// has to be Object because client can send anything. Implementation has to check/map/handle/refuse type
	public void receiveMessage(Object message, S session);



	public interface Provider<S extends NetworkSession<?>, H extends NetworkMessageReceiver<S>>
	{
		public H provideHandler();

		public void disposeHandler(H messageReceiver, Throwable cause);



		public class TrivialImplementation<S extends NetworkSession<?>, H extends NetworkMessageReceiver<S>>
		implements NetworkMessageReceiver.Provider<S, H>
		{
			private final H messageReceiver;

			public TrivialImplementation(final H messageReceiver)
			{
				super();
				this.messageReceiver = notNull(messageReceiver);
			}

			@Override
			public H provideHandler()
			{
				return this.messageReceiver;
			}

			@Override
			public void disposeHandler(final H messageReceiver, final Throwable cause)
			{
				// no-op
			}

		}

	}

}
