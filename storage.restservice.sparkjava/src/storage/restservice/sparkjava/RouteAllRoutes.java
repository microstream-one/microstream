package storage.restservice.sparkjava;

import java.util.Hashtable;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import spark.Request;
import spark.Response;

public class RouteAllRoutes extends RouteBase<StorageRestServiceDefault>
{
	public RouteAllRoutes(final StorageRestServiceDefault storageRestAdapter)
	{
		super(storageRestAdapter);
	}

	@Override
	public Object handle(final Request request, final Response response)
	{
		final Hashtable<String,Hashtable<String,String>> routes = this.storageRestAdapter.getRegisteredRoutes();

		final JsonArray routesJson = new JsonArray(routes.size());

		routes.forEach( (path,  methods ) -> {

			final JsonObject route = new JsonObject();
			route.addProperty("URL", request.host() +  path);

			final JsonArray methodsJson = new JsonArray(methods.size());
			route.add("HttpMethod", methodsJson);

			methods.forEach((method, handler) -> {
				methodsJson.add(method.toString());
			});

			routesJson.add(route);
		});

		response.type("application/json");

		return routesJson;
	}

}
