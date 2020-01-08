package one.microstream.viewer.server;

import java.util.Arrays;

import one.microstream.meta.XDebug;
import one.microstream.viewer.StorageRestAdapter;
import spark.Request;
import spark.Response;

public class RouteObjectMember extends RouteBase
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public RouteObjectMember(final StorageRestAdapter<String> embeddedStorageRestAdapter)
	{
		super(embeddedStorageRestAdapter);
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public String handle(final Request request, final Response response)
	{
		final long objectId = this.validateObjectId(request);
		final int requestedElementsCount = this.validateElementCount(request);
		final int[] memberPath = this.validateMemberPath(request);
		final int startMemberIndex = this.validateStartMemberIndex(request);

		XDebug.println("oid: " + objectId +
			" count: " + requestedElementsCount +
			" start: " + startMemberIndex +
			" path:  " + Arrays.toString(memberPath)
			);

		final String jsonString = this.getStorageRestAdapter().getObject(
			objectId,
			memberPath,
			requestedElementsCount,
			startMemberIndex);

		response.type("application/json");
		return jsonString;
	}

	/**
	 *
	 * @param request
	 * @return array of member indices, may be null
	 */
	private int[] validateMemberPath(final Request request)
	{
		if(request.splat().length > 0)
		{
			try
			{
				final String[] splats = request.splat()[0].split("/");
				final int[] indices = new int[splats.length];

				for(int i = 0; i < splats.length; i++)
				{
					indices[i] = Integer.parseInt(splats[i]);
				}

				return indices;
			}
			catch(final NumberFormatException | ArrayIndexOutOfBoundsException e )
			{
				throw new InvalidRouteParameters("invalid member indices");
			}
		}

		return null;
	}

	private int validateElementCount(final Request request)
	{
		try
		{
			return Integer.parseInt(request.params(":count"));
		}
		catch(final NumberFormatException e )
		{
			throw new InvalidRouteParameters("requested element count invalid");
		}
	}

	private int validateStartMemberIndex(final Request request)
	{
		if(request.params(":start") == null)
		{
			return 0;
		}

		try
		{
			return Integer.parseInt(request.params(":start"));
		}
		catch(final NumberFormatException e )
		{
			throw new InvalidRouteParameters("requested member start index invalid");
		}
	}
}
