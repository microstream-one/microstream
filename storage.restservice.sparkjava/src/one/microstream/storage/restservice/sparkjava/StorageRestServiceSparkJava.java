package one.microstream.storage.restservice.sparkjava;

import static one.microstream.X.notNull;

import one.microstream.storage.restadapter.StorageRestAdapter;
import one.microstream.storage.restadapter.StorageRestAdapterException;
import one.microstream.storage.restservice.StorageRestService;
import one.microstream.storage.types.StorageManager;
import spark.Service;
import spark.route.HttpMethod;

public class StorageRestServiceSparkJava implements StorageRestService
{
	public static StorageRestServiceSparkJava New(
		final StorageManager storage
	)
	{
		return new StorageRestServiceSparkJava(
			StorageRestAdapter.New(storage)
		);
	}
	
	public static StorageRestServiceSparkJava New(
		final StorageRestAdapter storageRestAdapter
	)
	{
		return new StorageRestServiceSparkJava(
			notNull(storageRestAdapter)
		);
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final StorageRestAdapter storageRestAdapter;
	private Service                  sparkService;
	private String                   storageName = "microstream";
	private RouteManager             routeManager;

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	StorageRestServiceSparkJava(
		final StorageRestAdapter storageRestAdapter
	)
	{
		super();
		this.storageRestAdapter = storageRestAdapter;
	}


	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	public void setInstanceName(final String name)
	{
		this.storageName = name;
	}

	public void setSparkService(final Service sparkService)
	{
		this.sparkService = sparkService;
	}

	public void setDefaultDataLength(final long defaultDataLength)
	{
		this.storageRestAdapter.setDefaultValueLength(defaultDataLength);
	}

	@Override
	public void start()
	{
		if(this.sparkService == null)
		{
			this.sparkService = Service.ignite();
		}

		this.setupRoutes();

		this.sparkService.init();
		this.sparkService.awaitInitialization();
	}

	private void setupRoutes()
	{
		this.routeManager = new DocumentationManager(this.sparkService);

		this.routeManager.registerRoute(HttpMethod.get, "/",
			new RouteAllRoutes((DocumentationManager) this.routeManager));

		this.routeManager.registerRoute(HttpMethod.get, "/" + this.storageName + "/root",
			new RouteGetRoot(this.storageRestAdapter));

		this.routeManager.registerRoute(HttpMethod.get, "/" + this.storageName + "/dictionary",
			new RouteTypeDictionary(this.storageRestAdapter));

		this.routeManager.registerRoute(HttpMethod.get, "/" + this.storageName + "/object/:oid",
			new RouteGetObject(this.storageRestAdapter));

		this.routeManager.registerRoute(HttpMethod.get, "/" + this.storageName + "/maintenance/filesStatistics",
			new RouteStorageFilesStatistics(this.storageRestAdapter));

		this.sparkService.exception(InvalidRouteParametersException.class, (e, request, response) ->
			{
				response.status(404);
				response.body(e.getMessage());
			});

		this.sparkService.exception(StorageRestAdapterException.class, (e, request, response) ->
			{
				response.status(404);
				response.body(e.getMessage());
			});
	}

	@Override
	public void stop()
	{
		if(this.sparkService != null)
		{
			this.sparkService.stop();
			this.sparkService.awaitStop();
		}
	}
}
