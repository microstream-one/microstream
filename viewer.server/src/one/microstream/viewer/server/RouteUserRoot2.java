package one.microstream.viewer.server;

import one.microstream.viewer.StorageRestAdapter2;
import spark.Request;
import spark.Response;
import spark.Route;

public class RouteUserRoot2 implements Route
{
	private final StorageRestAdapter2 storageRestAdapter;

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public RouteUserRoot2(final StorageRestAdapter2 embeddedStorageRestAdapter)
	{
		this.storageRestAdapter = embeddedStorageRestAdapter;
	}


	@Override
	public String handle(final Request request, final Response response)
	{
		final String jsonString = this.storageRestAdapter.getUserRoot();

		response.type("application/json");
		return jsonString;
	}

}
