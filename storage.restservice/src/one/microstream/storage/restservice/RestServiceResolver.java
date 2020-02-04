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

	public final static StorageRestServer getFirst(final EmbeddedStorageManager storage)
	{
		final ServiceLoader<StorageRestServer> serviceLoader = ServiceLoader.load(StorageRestServer.class);
		final StorageRestAdapter restAdapter = new StorageRestAdapter.Default(storage);

		for( final StorageRestServer server : serviceLoader)
		{
			return server.getInstance(restAdapter);
		}

		//TODO: better exception
		throw new RuntimeException("No StorageRestServer implementation found");
	}

	public final static <T extends StorageRestServer> T getType(final EmbeddedStorageManager storage, final Class<T> clazz)
	{
		final ServiceLoader<StorageRestServer> serviceLoader = ServiceLoader.load(StorageRestServer.class);
		final StorageRestAdapter restAdapter = new StorageRestAdapter.Default(storage);

		for( final StorageRestServer server : serviceLoader)
		{
			if(clazz.isInstance(server))
			{
				return clazz.cast(server.getInstance(restAdapter));
			}
		}

		//TODO: better exception
		throw new RuntimeException("No StorageRestServer implementation found");
	}

	public static StorageRestServer get(final EmbeddedStorageManager storage,  final Predicate<StorageRestServer> acceptor)
	{
		final ServiceLoader<StorageRestServer> serviceLoader = ServiceLoader.load(StorageRestServer.class);
		final StorageRestAdapter restAdapter = new StorageRestAdapter.Default(storage);

		for( final StorageRestServer server : serviceLoader)
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
