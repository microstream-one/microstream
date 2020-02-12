package storage.restservice.sparkjava;

import one.microstream.storage.restadapter.StorageRestAdapterStorageInfo;
import spark.Request;
import spark.Response;

public class RouteStorageFilesStatistics extends RouteBaseConvertable<StorageRestAdapterStorageInfo>
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
