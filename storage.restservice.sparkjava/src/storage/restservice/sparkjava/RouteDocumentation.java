package storage.restservice.sparkjava;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import one.microstream.storage.restadapter.ViewerException;
import spark.Request;
import spark.Response;

public class RouteDocumentation extends RouteBase<StorageRestServiceDefault>
{
	public RouteDocumentation(final StorageRestServiceDefault storageRestAdapter)
	{
		super(storageRestAdapter);
	}

	@Override
	public Object handle(final Request request, final Response response) throws Exception
	{
		final Hashtable<String, Hashtable<String, String>> routes = this.storageRestAdapter.getRegisteredRoutes();
		final Hashtable<String,String> methods = routes.get(request.uri());
		final String handlerName = methods.get(request.requestMethod().toLowerCase() );

		return this.getDetails(handlerName, response, request);
	}

	private JsonObject getDetails(final String clazz, final Response response, final Request request)
	{
		return this.returnOpenAPI(clazz, response, request);
	}


	private JsonObject returnOpenAPI(final String clazz, final Response response, final Request request)
	{
		try(final InputStream in = this.getClass().getResourceAsStream("/resources/onlineDocu.json");
			final BufferedReader reader = new BufferedReader(new InputStreamReader(in));)
		{
			final StringBuilder builder = new StringBuilder(in.available()*2);
			String read = null;
			while((read = reader.readLine()) != null)
			{
				builder.append(read);
			}

			response.type("application/json");

			final JsonObject g = new Gson().fromJson(builder.toString(), JsonObject.class);
			final JsonObject o = g.getAsJsonObject("handler").getAsJsonObject(clazz);

			return o;
		}
		catch(final Exception e )
		{
			throw new ViewerException("resource not found");
		}
	}
}
