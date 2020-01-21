package one.microstream.viewer.server;

import one.microstream.viewer.StorageRestAdapter2;
import one.microstream.viewer.ViewerRootDescription;
import spark.Request;
import spark.Response;
import spark.Route;

public class RouteUserRoot2 implements Route
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final StorageRestAdapter2 storageRestAdapter;


	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public RouteUserRoot2(final StorageRestAdapter2 embeddedStorageRestAdapter)
	{
		this.storageRestAdapter = embeddedStorageRestAdapter;
	}


	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public String handle(final Request request, final Response response)
	{
		final ViewerRootDescription rootDescription = this.storageRestAdapter.getUserRoot();

		response.type("application/json");

		return this.storageRestAdapter.getConverter("application/json").convert(rootDescription);
	}

}
