package storage.restservice.sparkjava;

import spark.Request;
import spark.Route;

public abstract class RouteBase<T> implements Route
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

	protected double getDoubleParameter(final Request request, final String name, final double defaultValue)
	{
		final String param = request.queryParams(name);

		if(param == null)
		{
			return defaultValue;
		}

		try
		{
			return Double.parseDouble(param);
		}
		catch(final NumberFormatException e)
		{
			throw new InvalidRouteParametersException("invalid url parameter " + name);
		}
	}

	protected int getIntParameter(final Request request, final String name, final int defaultValue)
	{
		final String param = request.queryParams(name);

		if(param == null)
		{
			return defaultValue;
		}

		try
		{
			return Integer.parseInt(param);
		}
		catch(final NumberFormatException e)
		{
			throw new InvalidRouteParametersException("invalid url parameter " + name);
		}
	}
}
