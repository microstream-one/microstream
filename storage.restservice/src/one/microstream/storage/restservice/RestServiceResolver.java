package one.microstream.storage.restservice;

import java.util.ServiceLoader;
import java.util.function.Predicate;

import one.microstream.storage.restadapter.StorageRestAdapter;
import one.microstream.storage.types.EmbeddedStorageManager;

public class RestServiceResolver
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public RestServiceResolver()
	{
		super();
	}

	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public final static StorageRestService getFirst(final EmbeddedStorageManager storage)
	{
		final ServiceLoader<StorageRestService> serviceLoader = ServiceLoader.load(StorageRestService.class);
		final StorageRestAdapter restAdapter = new StorageRestAdapter.Default(storage);

		for( final StorageRestService server : serviceLoader)
		{
			return server.getInstance(restAdapter);
		}

		//TODO: better exception
		throw new RuntimeException("No StorageRestServer implementation found");
	}

	public final static <T extends StorageRestService> T getType(final EmbeddedStorageManager storage, final Class<T> clazz)
	{
		final ServiceLoader<StorageRestService> serviceLoader = ServiceLoader.load(StorageRestService.class);
		final StorageRestAdapter restAdapter = new StorageRestAdapter.Default(storage);

		for( final StorageRestService server : serviceLoader)
		{
			if(clazz.isInstance(server))
			{
				return clazz.cast(server.getInstance(restAdapter));
			}
		}

		//TODO: better exception
		throw new RuntimeException("No StorageRestServer implementation found");
	}

	public static StorageRestService get(final EmbeddedStorageManager storage,  final Predicate<StorageRestService> acceptor)
	{
		final ServiceLoader<StorageRestService> serviceLoader = ServiceLoader.load(StorageRestService.class);
		final StorageRestAdapter restAdapter = new StorageRestAdapter.Default(storage);

		for( final StorageRestService server : serviceLoader)
		{
			if(acceptor.test(server))
			{
				return server.getInstance(restAdapter);
			}
		}

		//TODO: better exception
		throw new RuntimeException("No StorageRestServer implementation found");
	}

}
