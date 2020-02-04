package storage.restservice.ms;

import one.microstream.storage.restadapter.StorageRestAdapter;
import one.microstream.storage.restadapter.ViewerException;
import one.microstream.storage.types.EmbeddedStorageManager;
import spark.RouteImpl;
import spark.Service;
import spark.route.HttpMethod;

public class StorageRestService
{
	///////////////////////////////////////////////////////////////////////////
	// constants  //
	///////////////

	private static final String DEFAULT_STORAGE_NAME = "microstream";

	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Service sparkService;
	private final StorageRestAdapter storageRestAdapter;
	private final String storageName;

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/*
	 * Construct with default spark service instance
	 */
	public StorageRestService(final EmbeddedStorageManager storage)
	{
		this(storage, Service.ignite(), DEFAULT_STORAGE_NAME);
	}

	/*
	 * Construct with custom spark Service instance
	 */
	public StorageRestService(final EmbeddedStorageManager storage, final Service sparkService)
	{
		this(storage, sparkService, DEFAULT_STORAGE_NAME);
	}

	/*
	 * Construct with custom storageName and default spark service
	 */
	public StorageRestService(final EmbeddedStorageManager storage, final String storageName)
	{
		this(storage, Service.ignite(), storageName);
	}

	/*
	 * Construct with custom spark service instance and storage name
	 */
	public StorageRestService(final EmbeddedStorageManager storage, final Service sparkService, final String storageName)
	{
		super();
		this.storageName = storageName;
		this.sparkService = sparkService;
		this.storageRestAdapter = new StorageRestAdapter.Default(storage);

//		final StorageViewDataConverter jsonConverter = new StorageViewDataConverterJson();
//		this.storageRestAdapter.register(jsonConverter, "application/json");
//		this.storageRestAdapter.register(jsonConverter, "json");
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	/*
	 * Start the spark service if not already done
	 */
	public StorageRestService start()
	{
		this.sparkService.addRoute(HttpMethod.get, RouteImpl.create("/" + this.storageName + "/object/:oid",
			new RouteGetObject(this.storageRestAdapter)));

		this.sparkService.addRoute(HttpMethod.get, RouteImpl.create("/" + this.storageName + "/root",
			new RouteGetRoot(this.storageRestAdapter)));

		this.sparkService.addRoute(HttpMethod.get, RouteImpl.create("/" + this.storageName + "/dictionary",
			new RouteTypeDictionary(this.storageRestAdapter)));

        this.sparkService.addRoute(HttpMethod.get, RouteImpl.create("/" + this.storageName + "/maintenance/filesStatistics",
            new RouteStorageFilesStatistics(this.storageRestAdapter)));

		this.sparkService.exception(InvalidRouteParametersException.class, (e, request, response) ->
			{
				response.status(404);
				response.body(e.getMessage());
			} );

		this.sparkService.exception(ViewerException.class, (e, request, response) ->
			{
				response.status(404);
				response.body(e.getMessage());
			} );

		this.sparkService.init();
		this.sparkService.awaitInitialization();

		return this;
	}

	/*
	 * Shutdown spark service
	 */
	public void shutdown()
	{
		this.sparkService.stop();
		this.sparkService.awaitStop();
	}

	/*
	 * Set the default length of returned data
	 */
	public void setDefaultDataLength(final long defaultDataLength)
	{
        this.storageRestAdapter.setDefaultDataLength(defaultDataLength);
	}

}
