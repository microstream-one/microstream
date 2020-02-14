package storage.restservice.sparkjava;

import spark.Request;
import spark.Response;

public class RouteDocumentation extends RouteBase<RoutesManager>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public RouteDocumentation(final RoutesManager storageRestAdapter)
	{
		super(storageRestAdapter);
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public Object handle(final Request request, final Response response)
	{
		final String paramMethod = this.getStringParameter(request, "method");

		response.type("application/json");

		if(paramMethod != null)
		{
			return this.storageRestAdapter.getDocumentation(request.uri(), paramMethod);
		}
		return this.storageRestAdapter.getDocumentation(request.uri());
	}
}
