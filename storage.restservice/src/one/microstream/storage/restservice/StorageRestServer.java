package one.microstream.storage.restservice;

import one.microstream.storage.restadapter.StorageRestAdapter;

public interface StorageRestServer
{
	public StorageRestServer getInstance(final StorageRestAdapter restAdapter);
	public void start();
	public void stop();
}