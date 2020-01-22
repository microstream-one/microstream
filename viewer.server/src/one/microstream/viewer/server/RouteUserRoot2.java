package one.microstream.viewer.server;

import one.microstream.viewer.StorageRestAdapter2;
import one.microstream.viewer.ViewerRootDescription;
import spark.Request;
import spark.Response;
import spark.Route;

public class RouteUserRoot2 extends AbstractRoute implements Route
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public RouteUserRoot2(final StorageRestAdapter2 embeddedStorageRestAdapter)
	{
		super(embeddedStorageRestAdapter);
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
