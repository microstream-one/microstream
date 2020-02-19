package storage.restservice.sparkjava;

import one.microstream.storage.restadapter.StorageRestAdapter;
import one.microstream.storage.restadapter.ViewerException;
import one.microstream.storage.restservice.StorageRestService;
import spark.Service;
import spark.route.HttpMethod;

public class StorageRestServiceDefault implements StorageRestService
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private StorageRestAdapter storageRestAdapter;
	private Service sparkService;
	private String storageName ="microstream";
	private RouteManager routeManager;


	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageRestServiceDefault()
	{
		super();
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
		this.storageRestAdapter.setDefaultDataLength(defaultDataLength);
	}

	public void setupRoutes()
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

		this.sparkService.exception(ViewerException.class, (e, request, response) ->
			{
				response.status(404);
				response.body(e.getMessage());
			});
	}

	@Override
	public StorageRestService getInstance(final StorageRestAdapter restAdapter)
	{
		this.storageRestAdapter = restAdapter;
		return this;
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

	@Override
	public void stop()
	{
		this.sparkService.stop();
		this.sparkService.awaitStop();
	}
}
