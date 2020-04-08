package one.microstream.storage.restservice;

import one.microstream.storage.restadapter.StorageRestAdapter;

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
