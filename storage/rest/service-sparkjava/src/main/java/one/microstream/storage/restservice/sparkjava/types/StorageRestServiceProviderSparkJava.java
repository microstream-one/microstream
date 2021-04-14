package one.microstream.storage.restservice.sparkjava.types;

import one.microstream.storage.restadapter.types.StorageRestAdapter;
import one.microstream.storage.restservice.types.StorageRestService;
import one.microstream.storage.restservice.types.StorageRestServiceProvider;

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
