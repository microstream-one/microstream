package one.microstream.viewer.server;

import one.microstream.persistence.binary.types.ViewerException;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.viewer.StorageRestAdapter;
import one.microstream.viewer.StorageViewDataProcessorFlat;
import one.microstream.viewer.dataobjects.JSONConverter;
import spark.RouteImpl;
import spark.Service;
import spark.route.HttpMethod;

public class EmbeddedStorageViewer
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Service sparkService;
	private final StorageRestAdapter<String> embeddedStorageRestAdapter;

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/*
	 * Construct with default spark Service instance
	 */
	public EmbeddedStorageViewer(final EmbeddedStorageManager storage)
	{
		this.sparkService = Service.ignite();
		this.embeddedStorageRestAdapter = new StorageRestAdapter<>(storage, new JSONConverter(), new StorageViewDataProcessorFlat());
	}

	/*
	 * Construct with custom spark Service instance
	 */
	public EmbeddedStorageViewer(final EmbeddedStorageManager storage, final Service sparkService)
	{
		this.sparkService = sparkService;
		this.embeddedStorageRestAdapter = new StorageRestAdapter<>(storage, new JSONConverter(), new StorageViewDataProcessorFlat());
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	/*
	 * Start the spark service if not already done
	 */
	public EmbeddedStorageViewer start()
	{
		this.sparkService.staticFiles.location("resources/");
		this.sparkService.addRoute(HttpMethod.get, RouteImpl.create("/microstream/root",  					new RouteUserRoot(this.embeddedStorageRestAdapter)));
		this.sparkService.addRoute(HttpMethod.get, RouteImpl.create("/microstream/object/:oid",  			new RouteObject(this.embeddedStorageRestAdapter)));
		this.sparkService.addRoute(HttpMethod.get, RouteImpl.create("/microstream/object/:oid/:count/*", 	new RouteObjectMember(this.embeddedStorageRestAdapter)));
		this.sparkService.exception(InvalidRouteParameters.class, (e, request, response) ->
			{
				response.status(404);
				response.body("invalid route parameters!");
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
