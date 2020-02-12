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
