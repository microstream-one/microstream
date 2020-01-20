package one.microstream.viewer.server;

import one.microstream.viewer.StorageRestAdapter2;
import spark.Request;
import spark.Response;
import spark.Route;

public class RouteObject2 implements Route
{
	private final StorageRestAdapter2 storageRestAdapter;

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public RouteObject2(final StorageRestAdapter2 embeddedStorageRestAdapter)
	{
		this.storageRestAdapter = embeddedStorageRestAdapter;
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

		final long objectId = this.validateObjectId(request);
		final String jsonString = this.storageRestAdapter.getObject(
			objectId,
			dataOffset,
			dataLength,
			resolveReverences,
			referenceOffset,
			referenceLength);

		response.type("application/json");
		return jsonString;
	}

	private long validateObjectId(final Request request)
	{
		try
		{
			return Long.parseLong(request.params(":oid"));
		}
		catch(final NumberFormatException e )
		{
			throw new InvalidRouteParameters("ObjectId invalid");
		}
	}

	private boolean getBooleanParameter(final Request request, final String name, final boolean defaultValue)
	{
		final String param = request.queryParams(name);
		if(param == null)
		{
			return defaultValue;
		}

		return param.toLowerCase().contentEquals("true");

	}

	private long getLongParameter(final Request request, final String name, final long defaultValue)
	{
		final String param = request.queryParams(name);

		if(param == null)
		{
			return defaultValue;
		}

		try
		{
			return Long.parseLong(param);
		}
		catch(final NumberFormatException e)
		{
			throw new InvalidRouteParameters("invalid url parameter " + name);
		}
	}



}
