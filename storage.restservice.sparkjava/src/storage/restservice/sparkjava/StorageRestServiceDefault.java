package storage.restservice.sparkjava;

import java.util.Hashtable;

import one.microstream.storage.restadapter.StorageRestAdapter;
import one.microstream.storage.restadapter.ViewerException;
import one.microstream.storage.restservice.StorageRestService;
import spark.RouteImpl;
import spark.Service;
import spark.route.HttpMethod;

public class StorageRestServiceDefault implements StorageRestService
{

//	public class RouteDescription
//	{
//		public final HttpMethod httpMethod;
//		public final String handlerClassName;
//
//		public RouteDescription(final HttpMethod httpMethod, final String handlerClassName)
//		{
//			super();
//			this.httpMethod = httpMethod;
//			this.handlerClassName = handlerClassName;
//		}
//	}

	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private StorageRestAdapter storageRestAdapter;
	private Service sparkService;
	private String storageName ="microstream";

	private final Hashtable<String, Hashtable<String, String>> registeredRoots = new Hashtable<>();

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

	public void registerRoutes(final HttpMethod httpMethod, final String path, final RouteBase<?> route)
	{
		Hashtable<String, String> methods = this.registeredRoots.get(path);
		if(methods == null)
		{
			methods = new Hashtable<>();
			this.registeredRoots.put(path, methods);
		}
		methods.put(httpMethod.toString().toLowerCase(), route.getClass().getName());
		this.sparkService.addRoute(httpMethod, RouteImpl.create(path, route));


		methods.put(HttpMethod.options.toString().toLowerCase(), route.getClass().getName());
		this.sparkService.addRoute(httpMethod, RouteImpl.create(path, new RouteDocumentation(this)));

	}

	public  Hashtable<String, Hashtable<String, String>> getRegisteredRoutes()
	{
		return this.registeredRoots;
	}

	public void setupRoutes()
	{

		this.registerRoutes(HttpMethod.get, "/" + this.storageName,
			new RouteAllRoutes(this));

		this.registerRoutes(HttpMethod.options, "/" + this.storageName + "/*",
			new RouteDocumentation(this));

		this.registerRoutes(HttpMethod.get, "/" + this.storageName + "/root",
			new RouteGetRoot(this.storageRestAdapter));

		this.registerRoutes(HttpMethod.get, "/" + this.storageName + "/dictionary",
			new RouteTypeDictionary(this.storageRestAdapter));

		this.registerRoutes(HttpMethod.get, "/" + this.storageName + "/object/:oid",
			new RouteGetObject(this.storageRestAdapter));

		this.registerRoutes(HttpMethod.get, "/" + this.storageName + "/maintenance/filesStatistics",
			new RouteStorageFilesStatistics(this.storageRestAdapter));


//		this.sparkService.addRoute(HttpMethod.options, RouteImpl.create("/" + this.storageName,
//				new RouteDocumentation()));
//		this.sparkService.addRoute(HttpMethod.options, RouteImpl.create("/" + this.storageName + "/*",
//				new RouteDocumentation()));
//
//		this.sparkService.addRoute(HttpMethod.get, RouteImpl.create("/" + this.storageName  + "/object/:oid",
//			new RouteGetObject(this.storageRestAdapter)));
//
//		this.sparkService.addRoute(HttpMethod.get, RouteImpl.create("/" + this.storageName + "/root",
//			new RouteGetRoot(this.storageRestAdapter)));
//
//		this.sparkService.addRoute(HttpMethod.get, RouteImpl.create("/" + this.storageName + "/dictionary",
//			new RouteTypeDictionary(this.storageRestAdapter)));
//
//        this.sparkService.addRoute(HttpMethod.get, RouteImpl.create("/" + this.storageName + "/maintenance/filesStatistics",
//            new RouteStorageFilesStatistics(this.storageRestAdapter)));

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
