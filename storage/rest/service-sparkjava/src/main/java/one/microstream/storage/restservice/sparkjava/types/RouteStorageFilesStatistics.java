package one.microstream.storage.restservice.sparkjava.types;

import one.microstream.storage.restadapter.types.StorageRestAdapterStorageInfo;
import spark.Request;
import spark.Response;

public class RouteStorageFilesStatistics extends RouteBaseConvertable<StorageRestAdapterStorageInfo>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public RouteStorageFilesStatistics(final StorageRestAdapterStorageInfo apiAdapter)
	{
		super(apiAdapter);
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public Object handle(final Request request, final Response response)
	{
		final String requestedFormat = this.getStringParameter(request, "format");

		return this.toRequestedFormat(this.apiAdapter.getStorageFilesStatistics(),
			requestedFormat,
			response);
	}

}
