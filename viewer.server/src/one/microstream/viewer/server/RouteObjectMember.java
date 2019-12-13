package one.microstream.viewer.server;

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
		final int[] memberIndices = this.validateMemberIndices(request);


		final String jsonString = this.getStorageRestAdapter().getObject(
			objectId,
			requestedElementsCount,
			memberIndices);

		response.type("application/json");
		return jsonString;
	}

	private int[] validateMemberIndices(final Request request)
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
}
