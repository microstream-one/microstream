package one.microstream.storage.restservice.sparkjava.types;

/*-
 * #%L
 * microstream-storage-restservice-sparkjava
 * %%
 * Copyright (C) 2019 - 2021 MicroStream Software
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

import java.util.Hashtable;

import spark.RouteImpl;
import spark.Service;
import spark.route.HttpMethod;

public class RouteManager
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	protected final Service sparkService;

	/*
	 * Holds the handler for a httpMethod and path
	 * Hashtable<RouteURI, <Hashtable<HttpMethod, HandlerClassName>>
	 */
	private final Hashtable<String, Hashtable<String, String>> registeredRoutes;

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public RouteManager(final Service sparkService)
	{
		super();
		this.sparkService = sparkService;
		this.registeredRoutes = new Hashtable<>();
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	public Hashtable<String, Hashtable<String, String>> getRegisteredRoutes()
	{
		return this.registeredRoutes;
	}

	/**
	 * Register a route
	 */
	public void registerRoute(final HttpMethod httpMethod, final String uri, final RouteBase<?> route)
	{
		Hashtable<String, String> methods = this.registeredRoutes.get(uri);
		if(methods == null)
		{
			methods = new Hashtable<>();
			this.registeredRoutes.put(uri, methods);
		}
		methods.put(httpMethod.toString().toLowerCase(), route.getClass().getName());
		this.sparkService.addRoute(httpMethod, RouteImpl.create(uri, route));
	}

}
