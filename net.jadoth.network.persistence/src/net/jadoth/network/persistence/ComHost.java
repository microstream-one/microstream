package net.jadoth.network.persistence;

public interface ComHost
{
	public ComConfiguration configuration();
	
	/**
	 * Listens for incoming connections and relays them for processing.
	 */
	public void acceptConnections();
	
	public void start();
	
	public void stop();
	
	public boolean isRunning();
	
	/* (31.10.2018 TM)TODO: JET-44
	 * - ComConfiguration
	 * - Network Configuration (port and stuff)
	 * - A target for accepted connections
	 */
	
	
	public final class Implementation implements ComHost
	{

		@Override
		public ComConfiguration configuration()
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ComHost#configuration()
		}

		@Override
		public void acceptConnections()
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ComHost#acceptConnections()
		}

		@Override
		public void start()
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ComHost#start()
		}

		@Override
		public void stop()
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ComHost#stop()
		}

		@Override
		public boolean isRunning()
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ComHost#isRunning()
		}
		
	}
	
	
	public static ComHost.Creator Creator()
	{
		return new ComHost.Creator.Implementation();
	}
	
	public interface Creator
	{
		public ComHost createComHost(ComConfiguration configuration, int port, ComManager comManager);
		
		public final class Implementation implements ComHost.Creator
		{

			@Override
			public final ComHost createComHost(
				final ComConfiguration configuration,
				final int              port         ,
				final ComManager       comManager
			)
			{
				return new ComHost.Implementation();
			}
			
		}
	}
	
	
}
