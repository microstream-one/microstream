package one.microstream.viewer.server;

import one.microstream.viewer.StorageRestAdapter2;
import one.microstream.viewer.StorageViewDataConverter;
import one.microstream.viewer.ViewerObjectDescription;
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
		final String requestFormat = this.getStringParameter(request, "format");

		final long objectId = this.validateObjectId(request);
		final ViewerObjectDescription storageObject = this.storageRestAdapter.getObject(
			objectId,
			dataOffset,
			dataLength,
			resolveReverences,
			referenceOffset,
			referenceLength);

		if(requestFormat != null)
		{
			final StorageViewDataConverter converter = this.storageRestAdapter.getConverter(requestFormat);
			if(converter != null)
			{
				final String responseContentType = converter.getHtmlResponseContentType();

				if(responseContentType != null)
				{
					response.type(responseContentType);
				}

				return converter.convert(storageObject);
			}
			else
			{
				throw new InvalidRouteParameters("format invalid");
			}
		}

		response.type("application/json");
		return this.storageRestAdapter.getConverter("application/json").convert(storageObject);
	}

	private String getStringParameter(final Request request, final String name)
	{
		return request.queryParams(name);
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

		if(param.toLowerCase().contentEquals("true"))
		{
			return true;
		}
		else
		{
			throw new InvalidRouteParameters("invalid url parameter " + name);
		}

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
