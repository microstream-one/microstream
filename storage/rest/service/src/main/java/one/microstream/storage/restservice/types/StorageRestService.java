package one.microstream.storage.restservice.types;

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
	 * Start the service
	 */
	public void start();

	/**
	 * stop the service
	 */
	public void stop();
}