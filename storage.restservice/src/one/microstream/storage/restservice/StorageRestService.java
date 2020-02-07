package one.microstream.storage.restservice;

import one.microstream.storage.restadapter.StorageRestAdapter;

/**
 * Service Provider Interface for server implementations using the api
 * provided by StorageRestAdapter interface.
 *
 * Usage:
 * 1. create an own implementation of this interface
 * 2. load it by using the methods the the class RestServiceResolver
 *
 *
 */
public interface StorageRestService
{
	/**
	 * Return a StorageRestService instance initialized with the provided StorageRestAdapter.
	 * This method is required for the RestServiceResolver.
	 *
	 * @param restAdapter
	 * @return StorageRestService instance
	 */
	public StorageRestService getInstance(final StorageRestAdapter restAdapter);

	/**
	 * Start the service
	 */
	public void start();

	/**
	 * stop the service
	 */
	public void stop();
}