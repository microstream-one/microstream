package one.microstream.viewer.server;

import one.microstream.viewer.StorageRestAdapterTypeDictionary;
import spark.Request;
import spark.Response;
import spark.Route;

public class RouteTypeDictionary implements Route {

	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final StorageRestAdapterTypeDictionary storageRestAdapter;

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public RouteTypeDictionary(final StorageRestAdapterTypeDictionary storageRestAdapter)
	{
		this.storageRestAdapter = storageRestAdapter;
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
