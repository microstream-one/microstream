package one.microstream.storage.restservice.sparkjava.types;

import spark.Request;
import spark.Response;

public class RouteAllRoutes extends RouteBase<DocumentationManager>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public RouteAllRoutes(final DocumentationManager apiAdapter)
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

		String host = request.host();
		if(request.contextPath() != null)
		{
			host += request.contextPath();
		}

		return this.apiAdapter.getAllRoutes(host);
	}

}
