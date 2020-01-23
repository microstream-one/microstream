package one.microstream.viewer.server;

import one.microstream.viewer.StorageRestAdapter;
import one.microstream.viewer.ViewerObjectDescription;
import spark.Request;
import spark.Response;
import spark.Route;

public class RouteGetObject extends RouteBase implements Route
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public RouteGetObject(final StorageRestAdapter embeddedStorageRestAdapter)
	{
		super(embeddedStorageRestAdapter);
	}


	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public String handle(final Request request, final Response response)
	{
		final long dataOffset = this.getLongParameter(request, "dataOffset", 0);
		final long dataLength = this.getLongParameter(request, "dataLength", Long.MAX_VALUE);
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
