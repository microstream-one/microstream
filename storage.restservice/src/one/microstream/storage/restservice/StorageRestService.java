package one.microstream.storage.restservice;

import one.microstream.storage.restadapter.StorageRestAdapter;

public interface StorageRestService
{
	public StorageRestService getInstance(final StorageRestAdapter restAdapter);
	public void start();
	public void stop();
}