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
	// instance fields //
	////////////////////

	private final Service sparkService;
	private final StorageRestAdapter2 embeddedStorageRestAdapter;

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/*
	 * Construct with default spark Service instance
	 */
	public EmbeddedStorageViewer2(final EmbeddedStorageManager storage)
	{
		this(storage, Service.ignite());
	}

	/*
	 * Construct with custom spark Service instance
	 */
	public EmbeddedStorageViewer2(final EmbeddedStorageManager storage, final Service sparkService)
	{
		super();
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
		this.sparkService.addRoute(HttpMethod.get, RouteImpl.create("/microstream/object/:oid",	new RouteObject2(this.embeddedStorageRestAdapter)));
		this.sparkService.addRoute(HttpMethod.get, RouteImpl.create("/microstream/root",	    new RouteUserRoot2(this.embeddedStorageRestAdapter)));

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
