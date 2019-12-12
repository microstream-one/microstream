package one.microstream.viewer.server;

import one.microstream.viewer.StorageRestAdapter;
import spark.Request;
import spark.Response;
import spark.Route;

public class RouteUserRoot implements Route
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final StorageRestAdapter<String> storageRestAdapter;

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public RouteUserRoot(final StorageRestAdapter<String> embeddedStorageRestAdapter)
	{
		this.storageRestAdapter = embeddedStorageRestAdapter;
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public String handle(final Request request, final Response response) throws Exception
	{
		final String jsonString = this.storageRestAdapter.getRoot();

		response.type("application/json");
		return jsonString;
	}
}
