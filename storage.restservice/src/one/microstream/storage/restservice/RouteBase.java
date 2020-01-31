package one.microstream.storage.restservice;

import one.microstream.storage.restadapter.StorageRestAdapterConverter;
import one.microstream.storage.restadapter.StorageViewDataConverter;
import spark.Request;
import spark.Response;

public class RouteBase<T extends StorageRestAdapterConverter>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	protected final T storageRestAdapter;


	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public RouteBase(final T storageRestAdapter)
	{
		super();
		this.storageRestAdapter = storageRestAdapter;
	}


	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	public String toRequestedFormat(final Object object, final String requestedFormat, final Response response)
	{
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
			throw new InvalidRouteParametersException("format invalid");
		}

		response.type("application/json");
		return this.storageRestAdapter.getConverter("application/json").convert(object);
	}

	protected long validateObjectId(final Request request)
	{
		try
		{
			return Long.parseLong(request.params(":oid"));
		}
		catch(final NumberFormatException e )
		{
			throw new InvalidRouteParametersException("ObjectId invalid");
		}
	}

	protected boolean getBooleanParameter(final Request request, final String name, final boolean defaultValue)
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
		throw new InvalidRouteParametersException("invalid url parameter " + name);

	}

	protected long getLongParameter(final Request request, final String name, final long defaultValue)
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
			throw new InvalidRouteParametersException("invalid url parameter " + name);
		}
	}

	protected String getStringParameter(final Request request, final String name)
	{
		return request.queryParams(name);
	}

}