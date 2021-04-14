package one.microstream.storage.restservice.types;

import one.microstream.storage.restadapter.types.StorageRestAdapter;

@FunctionalInterface
public interface StorageRestServiceProvider
{
	/**
	 * Return a StorageRestService instance initialized with the provided StorageRestAdapter.
	 * This method is required for the RestServiceResolver.
	 *
	 * @param adapter
	 * @return StorageRestService instance
	 */
	public StorageRestService provideService(final StorageRestAdapter adapter);
}
