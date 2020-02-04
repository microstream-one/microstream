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

	private RestServiceResolver()
	{
		super();
	}

	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	/**
	 * Get the first found implementation of the StorageRestService interface
	 *
	 * @param storage
	 * @return StorageRestService instance
	 */
	public final static StorageRestService getFirst(final EmbeddedStorageManager storage)
	{
		final ServiceLoader<StorageRestService> serviceLoader = ServiceLoader.load(StorageRestService.class);
		final StorageRestAdapter restAdapter = new StorageRestAdapter.Default(storage);

		for( final StorageRestService server : serviceLoader)
		{
			return server.getInstance(restAdapter);
		}

		throw new RestServiceResolverException("No StorageRestServer implementation found");
	}

	/**
	 * get the first found StorageRestService implementation that is an instance of clazz paramter
	 *
	 * @param <T>
	 * @param storage
	 * @param clazz
	 * @return StorageRestService implementing instance of type clazz
	 */
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

		throw new RestServiceResolverException("No StorageRestServer implementation found for " + clazz.getName());
	}

	/**
	 * Get first StorageRestService implementation that is accepted by the provided boolean Predicate acceptor
	 *
	 * @param storage
	 * @param acceptor
	 * @return StorageRestService instance
	 */
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

		throw new RestServiceResolverException("No StorageRestServer implementation found");
	}

}
