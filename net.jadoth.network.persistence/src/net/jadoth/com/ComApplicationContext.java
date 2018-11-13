package net.jadoth.com;

import java.net.InetSocketAddress;

public interface ComApplicationContext<C>
{
	public InetSocketAddress provideAddress();

	public ComChannelAcceptor provideChannelAcceptor();
	
	public ComPersistenceAdaptor<C> providePersistenceAdaptor();
}
