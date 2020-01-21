package one.microstream.viewer.server;

import one.microstream.persistence.binary.types.ViewerException;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.viewer.StorageRestAdapter2;
import spark.RouteImpl;
import spark.Service;
import spark.route.HttpMethod;

public class EmbeddedStorageViewer2
{
	///////////////////////////////////////////////////////////////////////////
	// constants  //
	///////////////

	private static final String DEFAULT_STORAGE_NAME = "microstream";

	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Service sparkService;
	private final StorageRestAdapter2 embeddedStorageRestAdapter;
	private final String storageName;

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/*
	 * Construct with default spark service instance
	 */
	public EmbeddedStorageViewer2(final EmbeddedStorageManager storage)
	{
		this(storage, Service.ignite(), DEFAULT_STORAGE_NAME);
	}

	/*
	 * Construct with custom spark Service instance
	 */
	public EmbeddedStorageViewer2(final EmbeddedStorageManager storage, final Service sparkService)
	{
		this(storage, sparkService, DEFAULT_STORAGE_NAME);
	}

	/*
	 * Construct with custom storageName and default spark service
	 */
	public EmbeddedStorageViewer2(final EmbeddedStorageManager storage, final String storageName)
	{
		this(storage, Service.ignite(), storageName);
	}

	/*
	 * Construct with custom spark service instance and storage name
	 */
	public EmbeddedStorageViewer2(final EmbeddedStorageManager storage, final Service sparkService, final String storageName)
	{
		super();
		this.storageName = storageName;
		this.sparkService = sparkService;
		this.embeddedStorageRestAdapter = new StorageRestAdapter2(storage);
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	/*
	 * Start the spark service if not already done
	 */
	public EmbeddedStorageViewer2 start()
	{
		this.sparkService.addRoute(HttpMethod.get, RouteImpl.create("/" + this.storageName + "/object/:oid",	new RouteObject2(this.embeddedStorageRestAdapter)));
		this.sparkService.addRoute(HttpMethod.get, RouteImpl.create("/" + this.storageName + "/root",	   	new RouteUserRoot2(this.embeddedStorageRestAdapter)));
		this.sparkService.addRoute(HttpMethod.get, RouteImpl.create("/" + this.storageName + "/dictionary",	new RouteTypeDictionary(this.embeddedStorageRestAdapter)));

		this.sparkService.exception(InvalidRouteParameters.class, (e, request, response) ->
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

}
