package storage.restservice.sparkjava;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
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

		final JsonArray methodsJson = new JsonArray(methods.size());

		methods.forEach((method, handler) -> {

			methodsJson.add(this.returnAPI(handler, method));

		});

		response.type("application/json");
		return methodsJson;
	}




	private JsonObject returnAPI(final String handler, final String httpMethod)
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

			final JsonObject g = new Gson().fromJson(builder.toString(), JsonObject.class);
			final JsonObject o = g.getAsJsonObject("handler").getAsJsonObject(handler);
			final JsonObject m = o.getAsJsonObject(httpMethod);

			return m;
		}
		catch(final Exception e )
		{
			throw new ViewerException(e.getMessage());
		}
	}
}
