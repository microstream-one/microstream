package one.microstream.storage.restservice.sparkjava.types;

import one.microstream.storage.restadapter.types.StorageRestAdapterObject;
import one.microstream.storage.restadapter.types.ViewerObjectDescription;
import spark.Request;
import spark.Response;

public class RouteGetObject extends RouteBaseConvertable<StorageRestAdapterObject>
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
		final long    fixedOffset       = this.getLongParameter(request, "fixedOffset", 0);
		final long    fixedLength       = this.getLongParameter(request, "fixedLength", Long.MAX_VALUE);
		final long    variableOffset    = this.getLongParameter(request, "variableOffset", 0);
		final long    variableLength    = this.getLongParameter(request, "variableLength", Long.MAX_VALUE);
		final long    valueLength       = this.getLongParameter(request, "valueLength", this.apiAdapter.getDefaultValueLength());
		final boolean resolveReferences = this.getBooleanParameter(request, "references", false);
		final String  requestedFormat   = this.getStringParameter(request, "format");

		final long objectId = this.validateObjectId(request);
		final ViewerObjectDescription storageObject = this.apiAdapter.getObject(
			objectId,
			fixedOffset,
			fixedLength,
			variableOffset,
			variableLength,
			valueLength,
			resolveReferences
		);

		return this.toRequestedFormat(storageObject, requestedFormat, response);
	}



}
