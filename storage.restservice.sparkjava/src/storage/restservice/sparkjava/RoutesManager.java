package storage.restservice.sparkjava;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import one.microstream.storage.restadapter.ViewerException;
import spark.RouteImpl;
import spark.Service;
import spark.route.HttpMethod;


public class RoutesManager
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Service sparkService;
	private final Hashtable<String, Hashtable<String, String>> registeredRoutes;
	private final Hashtable<String, Hashtable<String, JsonElement>> documentations;

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public RoutesManager(final Service sparkService)
	{
		super();
		this.sparkService = sparkService;
		this.registeredRoutes = new Hashtable<>();
		this.documentations = new Hashtable<>();

		this.buildLiveDocumentation();
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	public Hashtable<String, Hashtable<String, String>> getRegisteredRoutes()
	{
		return this.registeredRoutes;
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

	public Object getDocumentation(final String uri, final String httpMethod)
	{
		try {
			final String handler = this.registeredRoutes.get(uri).get(httpMethod);
			return this.documentations.get(handler).get(httpMethod);
		}
		catch(final Exception e)
		{
			throw new ViewerException("No documentation found");
		}
	}

	public Object getDocumentation(final String uri)
	{
		final Hashtable<String, String> UriMethods = this.registeredRoutes.get(uri);

		final JsonObject docu = new JsonObject();

		UriMethods.forEach((a,b) -> {
			docu.add(a, this.documentations.get(b).get(a));
		});

		return docu;
	}

	private String getResourceFileContentAsString(final String path)
	{
		try(final InputStream in = this.getClass().getResourceAsStream(path);
			final BufferedReader reader = new BufferedReader(new InputStreamReader(in));)
		{
			final StringBuilder builder = new StringBuilder(in.available()*2);
			String read = null;
			while((read = reader.readLine()) != null)
			{
				builder.append(read);
			}

			return builder.toString();
		}

		catch(final Exception e )
		{
			throw new ViewerException(e.getMessage());
		}
	}

	private void buildLiveDocumentation()
	{
		final String doc = this.getResourceFileContentAsString("/resources/onlineDocu.json");

		final JsonObject docu = new Gson().fromJson(doc, JsonObject.class);
		final JsonObject handlers = docu.getAsJsonObject("handler");

		handlers.entrySet().forEach( handler -> {

			Hashtable<String, JsonElement> handlerMethods = this.documentations.get(handler.getKey());
			if(handlerMethods == null)
			{
				handlerMethods = new Hashtable<>();
				this.documentations.put(handler.getKey(), handlerMethods);
			}

			final JsonObject methods =  handler.getValue().getAsJsonObject();

			final Set<String> key = methods.keySet();
			for (final String string : key)
			{
				handlerMethods.put(string, methods.get(string));
			}
		});
	}
}
