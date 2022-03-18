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

import one.microstream.storage.restadapter.types.StorageRestAdapterStorageInfo;
import spark.Request;
import spark.Response;

public class RouteStorageFilesStatistics extends RouteBaseConvertable<StorageRestAdapterStorageInfo>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public RouteStorageFilesStatistics(final StorageRestAdapterStorageInfo apiAdapter)
	{
		super(apiAdapter);
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public Object handle(final Request request, final Response response)
	{
		final String requestedFormat = this.getStringParameter(request, "format");

		return this.toRequestedFormat(this.apiAdapter.getStorageFilesStatistics(),
			requestedFormat,
			response);
	}

}
