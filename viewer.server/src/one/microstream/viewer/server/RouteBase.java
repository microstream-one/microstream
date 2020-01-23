package one.microstream.viewer.server;

import one.microstream.viewer.StorageRestAdapter;
import one.microstream.viewer.StorageViewDataConverter;
import spark.Request;
import spark.Response;

public class RouteBase
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	protected final StorageRestAdapter storageRestAdapter;


	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public RouteBase(final StorageRestAdapter embeddedStorageRestAdapter)
	{
		super();
		this.storageRestAdapter = embeddedStorageRestAdapter;
	}


	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	public String toRequestedFormat(final Object object, final String requestedFormat, final Response response) {
		if(requestedFormat != null)
		{
			final StorageViewDataConverter converter = this.storageRestAdapter.getConverter(requestedFormat);
			if(converter != null)
			{
				final String responseContentType = converter.getHtmlResponseContentType();

				if(responseContentType != null)
				{
					response.type(responseContentType);
				}

				return converter.convert(object);
			}
			else
			{
				throw new InvalidRouteParametersException("format invalid");
			}
		}

		response.type("application/json");
		return this.storageRestAdapter.getConverter("application/json").convert(object);
	}

	protected long validateObjectId(final Request request) {
		try
		{
			return Long.parseLong(request.params(":oid"));
		}
		catch(final NumberFormatException e )
		{
			throw new InvalidRouteParametersException("ObjectId invalid");
		}
	}

	protected boolean getBooleanParameter(final Request request, final String name, final boolean defaultValue) {
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
			throw new InvalidRouteParametersException("invalid url parameter " + name);
		}

	}

	protected long getLongParameter(final Request request, final String name, final long defaultValue) {
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
			throw new InvalidRouteParametersException("invalid url parameter " + name);
		}
	}

	protected String getStringParameter(final Request request, final String name)
	{
		return request.queryParams(name);
	}

}