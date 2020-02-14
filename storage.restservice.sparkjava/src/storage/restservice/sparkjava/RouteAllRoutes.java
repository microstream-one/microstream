package storage.restservice.sparkjava;

import spark.Request;
import spark.Response;

public class RouteAllRoutes extends RouteBase<RoutesManager>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public RouteAllRoutes(final RoutesManager storageRestAdapter)
	{
		super(storageRestAdapter);
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public Object handle(final Request request, final Response response)
	{
		response.type("application/json");
		return this.storageRestAdapter.getAllRoutes(request.host());
	}

}
