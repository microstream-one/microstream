package net.jadoth.network.types;

import static net.jadoth.Jadoth.notNull;

public interface NetworkSessionClientGreeter<S extends NetworkSession<?>>
{
	public void greetClient(S session);


	public interface Provider<S extends NetworkSession<?>>
	{
		public NetworkSessionClientGreeter<S> provideClientGreeter();

		public void disposeClientGreeter(NetworkSessionClientGreeter<S> clientGreeter, Throwable cause);



		public class TrivialImplementation<S extends NetworkSession<?>>
		implements NetworkSessionClientGreeter.Provider<S>
		{
			private final NetworkSessionClientGreeter<S> clientGreeter;

			public TrivialImplementation(final NetworkSessionClientGreeter<S> clientGreeter)
			{
				super();
				this.clientGreeter = notNull(clientGreeter);
			}

			@Override
			public NetworkSessionClientGreeter<S> provideClientGreeter()
			{
				return this.clientGreeter;
			}

			@Override
			public void disposeClientGreeter(final NetworkSessionClientGreeter<S> clientGreeter, final Throwable cause)
			{
				// no-op
			}

		}
	}

}
