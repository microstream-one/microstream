package one.microstream.viewer.server;

import one.microstream.viewer.StorageRestAdapter;
import spark.Request;
import spark.Response;

public class RouteObject extends RouteBase
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public RouteObject(final StorageRestAdapter<String> embeddedStorageRestAdapter)
	{
		super(embeddedStorageRestAdapter);
	}


	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public String handle(final Request request, final Response response)
	{
		final long objectId = this.validateObjectId(request);

		final String jsonString = this.getStorageRestAdapter().getObject(objectId);

		response.type("application/json");
		return jsonString;
	}
}
