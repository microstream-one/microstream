package one.microstream.storage.restservice.sparkjava;

import one.microstream.storage.restadapter.StorageRestAdapter;
import one.microstream.storage.restservice.StorageRestService;
import one.microstream.storage.restservice.StorageRestServiceProvider;

public class StorageRestServiceProviderSparkJava implements StorageRestServiceProvider
{
	public StorageRestServiceProviderSparkJava()
	{
		super();
	}

	@Override
	public StorageRestService provideService(
		final StorageRestAdapter adapter
	)
	{
		return StorageRestServiceSparkJava.New(adapter);
	}
	
}
