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


public class DocumentationManager extends RouteManager
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	/*
	 * Hold the documentation for a handler's route and http method
	 * Hashtable<HandlerClassName, <Hashtable<HttpMethod, JsonDocuPart>>
	 */
	private final Hashtable<String, Hashtable<String, JsonElement>> documentations;

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public DocumentationManager(final Service sparkService)
	{
		super(sparkService);
		this.documentations = new Hashtable<>();

		this.buildLiveDocumentation("/resources/onlineDocu.json");
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	/**
	 * Register a route / httpMethod and automatically create and register an options route
	 * to get help on this route
	 */
	@Override
	public void registerRoute(final HttpMethod httpMethod, final String uri, final RouteBase<?> route)
	{
		super.registerRoute(httpMethod, uri, route);

		final Hashtable<String, String> methods = this.getRegisteredRoutes().get(uri);
		methods.put(HttpMethod.options.toString().toLowerCase(), route.getClass().getName());
        this.sparkService.addRoute(HttpMethod.options, RouteImpl.create(uri, new RouteDocumentation(this)));
	}

	/**
	 * get a Json Array containing all registered roots and there httpMethods
	 *
	 * @param host: the host url and context path
	 *
	 * @return JsonArray
	 */
	public Object getAllRoutes(final String host)
	{
		final JsonArray routesJson = new JsonArray(this.getRegisteredRoutes().size());

		this.getRegisteredRoutes().forEach( (path,  methods ) -> {

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

	/**
	 * Get the documentation snippet for a http method for a registered uri
	 *
	 * @param uri
	 * @param httpMethod
	 * @return JsonObject
	 */
	public Object getDocumentation(final String uri, final String httpMethod)
	{
		try {
			final String handler = this.getRegisteredRoutes().get(uri).get(httpMethod);
			return this.documentations.get(handler).get(httpMethod);
		}
		catch(final Exception e)
		{
			throw new ViewerException("No documentation found");
		}
	}

	/**
	 * Get the documentation snippet of all http methods for a registered uri
	 *
	 * @param uri
	 * @param httpMethod
	 * @return JsonObject
	 */
	public Object getDocumentation(final String uri)
	{
		final Hashtable<String, String> UriMethods = this.getRegisteredRoutes().get(uri);

		final JsonObject docu = new JsonObject();

		UriMethods.forEach((httpMethod, handlerName) -> {
			docu.add(httpMethod, this.documentations.get(handlerName).get(httpMethod));
		});

		return docu;
	}

	/**
	 * Get a file resource content as String
	 *
	 * @param uri
	 * @return content as String
	 */
	private String getResourceFileContentAsString(final String uri)
	{
		try(final InputStream in = this.getClass().getResourceAsStream(uri);
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

	/**
	 * Build the documentation from an provided embedded json resource file
	 *
	 */
	private void buildLiveDocumentation(final String FileURI)
	{
		final String doc = this.getResourceFileContentAsString(FileURI);

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
