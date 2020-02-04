package storage.restservice.ms;

import one.microstream.storage.restadapter.StorageRestAdapterObject;
import one.microstream.storage.restadapter.ViewerObjectDescription;
import spark.Request;
import spark.Response;
import spark.Route;

public class RouteGetObject extends RouteBase<StorageRestAdapterObject> implements Route
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public RouteGetObject(final StorageRestAdapterObject storageRestAdapter)
	{
		super(storageRestAdapter);
	}


	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public String handle(final Request request, final Response response)
	{
		final long dataOffset = this.getLongParameter(request, "dataOffset", 0);
        final long dataLength = this.getLongParameter(request, "dataLength", this.storageRestAdapter.getDefaultDataLength());
		final long referenceOffset = this.getLongParameter(request, "referenceOffset", 0);
		final long referenceLength = this.getLongParameter(request, "referenceLength", Long.MAX_VALUE);
		final boolean resolveReverences = this.getBooleanParameter(request, "references", false);
		final String requestedFormat = this.getStringParameter(request, "format");

		final long objectId = this.validateObjectId(request);
		final ViewerObjectDescription storageObject = this.storageRestAdapter.getObject(
			objectId,
			dataOffset,
			dataLength,
			resolveReverences,
			referenceOffset,
			referenceLength);

		return this.toRequestedFormat(storageObject, requestedFormat, response);
	}



}
