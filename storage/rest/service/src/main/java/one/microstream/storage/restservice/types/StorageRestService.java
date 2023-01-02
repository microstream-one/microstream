package one.microstream.storage.restservice.types;

/*-
 * #%L
 * microstream-storage-restservice
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

/**
 * Service Provider Interface for server implementations using the api
 * provided by StorageRestAdapter interface.
 * <p/>
 * Usage:
 * 1. create an own implementation of this interface
 * 2. load it by using the methods the the class RestServiceResolver
 *
 */
public interface StorageRestService
{
	/**
	 * Start the service
	 */
	public void start();

	/**
	 * stop the service
	 */
	public void stop();
}
