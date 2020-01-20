package one.microstream.viewer.server;

import one.microstream.viewer.StorageRestAdapter2;
import spark.Request;
import spark.Response;
import spark.Route;

public class RouteTypeDictionary implements Route {

	private final StorageRestAdapter2 storageRestAdapter;

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public RouteTypeDictionary(final StorageRestAdapter2 embeddedStorageRestAdapter)
	{
		this.storageRestAdapter = embeddedStorageRestAdapter;
	}


	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public String handle(final Request request, final Response response)
	{
		final String string = this.storageRestAdapter.getTypeDictionary();
		response.type("application/text");
		return string;
	}

}
