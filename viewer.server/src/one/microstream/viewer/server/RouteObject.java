package one.microstream.viewer.server;

import one.microstream.viewer.StorageRestAdapter;
import spark.Request;
import spark.Response;
import spark.Route;

public class RouteObject implements Route
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final StorageRestAdapter<String> storageRestAdapter;

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public RouteObject(final StorageRestAdapter<String> embeddedStorageRestAdapter)
	{
		this.storageRestAdapter = embeddedStorageRestAdapter;
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public String handle(final Request request, final Response response) throws Exception
	{
		//get named parameter
		final String paramOid = request.params(":oid");

		final String jsonString = this.storageRestAdapter.getObject(
			Long.parseLong(paramOid));

		response.type("application/json");
		return jsonString;
	}
}
