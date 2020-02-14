package storage.restservice.sparkjava;

import spark.Request;
import spark.Response;

public class RouteAllRoutes extends RouteBase<RoutesManager>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public RouteAllRoutes(final RoutesManager apiAdapter)
	{
		super(apiAdapter);
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public Object handle(final Request request, final Response response)
	{
		response.type("application/json");
		return this.apiAdapter.getAllRoutes(request.host());
	}

}
