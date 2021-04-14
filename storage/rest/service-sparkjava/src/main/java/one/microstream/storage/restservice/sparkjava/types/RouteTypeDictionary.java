package one.microstream.storage.restservice.sparkjava.types;

import one.microstream.storage.restadapter.types.StorageRestAdapterTypeDictionary;
import spark.Request;
import spark.Response;

public class RouteTypeDictionary extends RouteBase<StorageRestAdapterTypeDictionary>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public RouteTypeDictionary(final StorageRestAdapterTypeDictionary apiAdapter)
	{
		super(apiAdapter);
	}


	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public String handle(final Request request, final Response response)
	{
		final String string = this.apiAdapter.getTypeDictionary();
		response.type("application/text");
		return string;
	}

}
