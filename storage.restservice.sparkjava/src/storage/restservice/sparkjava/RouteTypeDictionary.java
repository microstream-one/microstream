package storage.restservice.sparkjava;

import one.microstream.storage.restadapter.StorageRestAdapterTypeDictionary;
import spark.Request;
import spark.Response;

public class RouteTypeDictionary extends RouteBase<StorageRestAdapterTypeDictionary>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public RouteTypeDictionary(final StorageRestAdapterTypeDictionary storageRestAdapter)
	{
		super(storageRestAdapter);
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
