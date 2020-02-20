package storage.restservice.sparkjava;

import one.microstream.storage.restadapter.StorageRestAdapterObject;
import one.microstream.storage.restadapter.ViewerObjectDescription;
import spark.Request;
import spark.Response;

public class RouteGetObject extends RouteBaseConvertable<StorageRestAdapterObject>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public RouteGetObject(final StorageRestAdapterObject apiAdapter)
	{
		super(apiAdapter);
	}


	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public String handle(final Request request, final Response response)
	{
		final long dataOffset = this.getLongParameter(request, "dataOffset", 0);
        final long dataLength = this.getLongParameter(request, "dataLength", this.apiAdapter.getDefaultDataLength());
		final long referenceOffset = this.getLongParameter(request, "referenceOffset", 0);
		final long referenceLength = this.getLongParameter(request, "referenceLength", Long.MAX_VALUE);
		final long valueOffset = this.getLongParameter(request, "valueOffset", 0);
		final long valueLength = this.getLongParameter(request, "valueLength", Long.MAX_VALUE);
		final boolean resolveReferences = this.getBooleanParameter(request, "references", false);
		final String requestedFormat = this.getStringParameter(request, "format");

		final long objectId = this.validateObjectId(request);
		final ViewerObjectDescription storageObject = this.apiAdapter.getObject(
			objectId,
			dataOffset,
			dataLength,
			valueOffset,
			valueLength,
			resolveReferences,
			referenceOffset,
			referenceLength);

		return this.toRequestedFormat(storageObject, requestedFormat, response);
	}
}
