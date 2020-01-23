package one.microstream.viewer.server;

import one.microstream.viewer.StorageRestAdapterRoot;
import one.microstream.viewer.ViewerRootDescription;
import spark.Request;
import spark.Response;
import spark.Route;

public class RouteGetRoot extends RouteBase<StorageRestAdapterRoot> implements Route
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public RouteGetRoot(final StorageRestAdapterRoot storageRestAdapter)
	{
		super(storageRestAdapter);
	}


	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public String handle(final Request request, final Response response)
	{
		final String requestedFormat = this.getStringParameter(request, "format");
		final ViewerRootDescription rootDescription = this.storageRestAdapter.getUserRoot();

		return this.toRequestedFormat(rootDescription, requestedFormat, response);
	}

}
