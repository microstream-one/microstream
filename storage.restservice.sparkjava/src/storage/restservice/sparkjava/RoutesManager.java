package storage.restservice.sparkjava;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import one.microstream.storage.restadapter.ViewerException;
import spark.RouteImpl;
import spark.Service;
import spark.route.HttpMethod;

public class RoutesManager
{
	private final Service sparkService;
	private final Hashtable<String, Hashtable<String, String>> registeredRoutes;

	public RoutesManager(final Service sparkService)
	{
		super();
		this.sparkService = sparkService;
		this.registeredRoutes = new Hashtable<>();
	}

	public void registerRoutes(final HttpMethod httpMethod, final String path, final RouteBase<?> route)
	{
		Hashtable<String, String> methods = this.registeredRoutes.get(path);
		if(methods == null)
		{
			methods = new Hashtable<>();
			this.registeredRoutes.put(path, methods);
		}
		methods.put(httpMethod.toString().toLowerCase(), route.getClass().getName());
		this.sparkService.addRoute(httpMethod, RouteImpl.create(path, route));


		methods.put(HttpMethod.options.toString().toLowerCase(), route.getClass().getName());
		this.sparkService.addRoute(HttpMethod.options, RouteImpl.create(path, new RouteDocumentation(this)));

	}

	public Hashtable<String, Hashtable<String, String>> getRegisteredRoutes()
	{
		return this.registeredRoutes;
	}

	public Object getDocumentation(final String uri)
	{
//		//TODO: Error handling
		final Hashtable<String,String> methods = this.registeredRoutes.get(uri);

		final JsonArray methodsJson = new JsonArray(methods.size());

		methods.forEach((method, handler) -> {

			methodsJson.add(this.returnAPI(handler, method));

		});

		return methodsJson;
	}

	public Object getAllRoutes(final String host)
	{
		final JsonArray routesJson = new JsonArray(this.registeredRoutes.size());

		this.registeredRoutes.forEach( (path,  methods ) -> {

			final JsonObject route = new JsonObject();
			route.addProperty("URL", host +  path);

			final JsonArray methodsJson = new JsonArray(methods.size());
			route.add("HttpMethod", methodsJson);

			methods.forEach((method, handler) -> {
				methodsJson.add(method.toString());
			});

			routesJson.add(route);
		});

		return routesJson;
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
		//TODO: throw different exceptions for io related issues and not available for handler ...
		catch(final Exception e )
		{
			throw new ViewerException(e.getMessage());
		}
	}
}
