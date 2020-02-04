package storage.restservice.ms;

import one.microstream.storage.restadapter.StorageRestAdapterStorageInfo;
import spark.Request;
import spark.Response;
import spark.Route;

public class RouteStorageFilesStatistics extends RouteBase<StorageRestAdapterStorageInfo> implements Route
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public RouteStorageFilesStatistics(final StorageRestAdapterStorageInfo storageRestAdapter)
	{
		super(storageRestAdapter);
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public Object handle(final Request request, final Response response)
	{
		final String requestedFormat = this.getStringParameter(request, "format");

		return this.toRequestedFormat(this.storageRestAdapter.getStorageFilesStatistics(),
			requestedFormat,
			response);
	}

}
