package one.microstream.viewer.server;

import one.microstream.viewer.StorageRestAdapter;
import spark.Request;
import spark.Response;

public class RouteRoots extends RouteBase
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public RouteRoots(final StorageRestAdapter<String> embeddedStorageRestAdapter)
	{
		super(embeddedStorageRestAdapter);
	}


	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public String handle(final Request request, final Response response)
	{
		final String jsonString = this.getStorageRestAdapter().getAllRoots();

		response.type("application/json");
		return jsonString;
	}
}
