package one.microstream.storage.restservice.sparkjava.types;

/*-
 * #%L
 * microstream-storage-restservice-sparkjava
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import one.microstream.storage.restadapter.exceptions.StorageRestAdapterException;
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

		this.buildLiveDocumentation("/doc.json");
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	/**
	 * Registers a route / httpMethod and automatically creates and registers an options route
	 * to get help on this route.
	 * 
	 * @param httpMethod the http method
	 * @param uri the uri
	 * @param route the route
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
	 * Get a Json Array containing all registered roots and there httpMethods.
	 *
	 * @param host the host url and context path
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
	 * Get the documentation snippet for a http method for a registered uri.
	 *
	 * @param uri the uri
	 * @param httpMethod the http method
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
			throw new StorageRestAdapterException("No documentation found");
		}
	}

	/**
	 * Get the documentation snippet of all http methods for a registered uri.
	 *
	 * @param uri the uri
	 * @return JsonObject
	 */
	public Object getDocumentation(final String uri)
	{
		final Hashtable<String, String> UriMethods = this.getRegisteredRoutes().get(uri);
		if(UriMethods == null)
		{
			throw new StorageRestAdapterException("No documentation found");
		}

		final JsonObject docu = new JsonObject();

		UriMethods.forEach((httpMethod, handlerName) -> {
			docu.add(httpMethod, this.documentations.get(handlerName).get(httpMethod));
		});

		return docu;
	}

	/**
	 * Build the documentation from an provided embedded json resource file.
	 * 
	 * @param filePath the file path
	 */
	private void buildLiveDocumentation(final String filePath)
	{
		final JsonObject doc;
		try(final BufferedReader reader = new BufferedReader(new InputStreamReader(
			this.getClass().getResourceAsStream(filePath)
		)))
		{
			doc = new Gson().fromJson(reader, JsonObject.class);
		}
		catch(final Exception e )
		{
			throw new StorageRestAdapterException(e);
		}

		final JsonObject handlers = doc.getAsJsonObject("handler");

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
