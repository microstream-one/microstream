package storage.restservice.sparkjava;

import one.microstream.storage.restadapter.StorageRestAdapterRoot;
import one.microstream.storage.restadapter.ViewerRootDescription;
import spark.Request;
import spark.Response;

public class RouteGetRoot extends RouteBaseConvertable<StorageRestAdapterRoot>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public RouteGetRoot(final StorageRestAdapterRoot apiAdapter)
	{
		super(apiAdapter);
	}


	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public String handle(final Request request, final Response response)
	{
		final String requestedFormat = this.getStringParameter(request, "format");
		final ViewerRootDescription rootDescription = this.apiAdapter.getUserRoot();

		return this.toRequestedFormat(rootDescription, requestedFormat, response);
	}

}
