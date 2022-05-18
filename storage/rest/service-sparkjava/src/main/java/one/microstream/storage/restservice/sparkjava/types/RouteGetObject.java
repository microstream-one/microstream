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

import one.microstream.storage.restadapter.types.StorageRestAdapterObject;
import one.microstream.storage.restadapter.types.ViewerObjectDescription;
import spark.Request;
import spark.Response;

public class RouteGetObject extends RouteBaseConvertable<StorageRestAdapterObject>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public RouteGetObject(final StorageRestAdapterObject storageRestAdapter)
	{
		super(storageRestAdapter);
	}


	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public String handle(final Request request, final Response response)
	{
		final long    fixedOffset       = this.getLongParameter(request, "fixedOffset", 0);
		final long    fixedLength       = this.getLongParameter(request, "fixedLength", Long.MAX_VALUE);
		final long    variableOffset    = this.getLongParameter(request, "variableOffset", 0);
		final long    variableLength    = this.getLongParameter(request, "variableLength", Long.MAX_VALUE);
		final long    valueLength       = this.getLongParameter(request, "valueLength", this.apiAdapter.getDefaultValueLength());
		final boolean resolveReferences = this.getBooleanParameter(request, "references", false);
		final String  requestedFormat   = this.getStringParameter(request, "format");

		final long objectId = this.validateObjectId(request);
		final ViewerObjectDescription storageObject = this.apiAdapter.getObject(
			objectId,
			fixedOffset,
			fixedLength,
			variableOffset,
			variableLength,
			valueLength,
			resolveReferences
		);

		return this.toRequestedFormat(storageObject, requestedFormat, response);
	}



}
