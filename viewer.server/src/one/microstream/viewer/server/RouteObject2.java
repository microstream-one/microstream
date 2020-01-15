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
		final long dataOffset = this.getLongParamter(request, "dataOffset");
		final long dataLength = this.getLongParamter(request, "dataLength");
		final long referenceOffset = this.getLongParamter(request, "referenceOffset");
		final long referenceLength = this.getLongParamter(request, "referenceLength");

		final long objectId = this.validateObjectId(request);
		final String jsonString = this.storageRestAdapter.getObject(objectId, dataOffset , dataLength, referenceOffset, referenceLength);

		response.type("application/json");
		return jsonString;
	}

	protected long validateObjectId(final Request request)
	{
		try
		{
			return Long.parseLong(request.params(":oid"));
		}
		catch(final NumberFormatException e )
		{
			throw new InvalidRouteParameters("Object Id invalid");
		}
	}

	protected long getLongParamter(final Request request, final String name)
	{
		final String param = request.queryParams(name);

		if(param == null)
		{
			return -1;
		}

		try
		{
			return Long.parseLong(param);
		}
		catch(final NumberFormatException e)
		{
			throw new InvalidRouteParameters("invalid parameter" + name);
		}
	}



}
