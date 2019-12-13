package one.microstream.viewer.server;

import java.util.regex.PatternSyntaxException;

import one.microstream.viewer.StorageRestAdapter;
import spark.Request;
import spark.Response;
import spark.Route;

public class RouteObjectMember implements Route
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final StorageRestAdapter<String> storageRestAdapter;

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public RouteObjectMember(final StorageRestAdapter<String> embeddedStorageRestAdapter)
	{
		this.storageRestAdapter = embeddedStorageRestAdapter;
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public String handle(final Request request, final Response response)
	{
		//get named parameter
		long objectId = 0;
		int requestedElementsCount = 0;
		int[] subRoute = null;

		try
		{
			objectId = Long.parseLong(request.params(":oid"));
			requestedElementsCount = Integer.parseInt(request.params(":count"));
		}
		catch(final NumberFormatException e)
		{
			throw new InvalidRouteParameters();
		}

		try
		{
			final String[] splats = request.splat()[0].split("/");
			subRoute = new int[splats.length];
			for(int i = 0; i < splats.length; i++)
			{
				subRoute[i] = Integer.parseInt(splats[i]);
			}

		}
		catch(final NumberFormatException | NullPointerException | PatternSyntaxException  e)
		{
			throw new InvalidRouteParameters();
		}

		final String jsonString = this.storageRestAdapter.getObject(
			objectId,
			requestedElementsCount,
			subRoute);

		response.type("application/json");
		return jsonString;
	}
}
