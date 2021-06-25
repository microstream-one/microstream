package one.microstream.storage.restadapter.types;

/*-
 * #%L
 * microstream-storage-restadapter
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

import one.microstream.persistence.types.Persistence;

public class ObjectReferenceWrapper
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private long objectId;


	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ObjectReferenceWrapper(final long objectId)
	{
		super();
		this.setObjectId(objectId);
	}


	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	public long getObjectId()
	{
		return this.objectId;
	}

	public void setObjectId(final long objectId)
	{
		this.objectId = objectId;
	}

	public boolean isValidObjectReference()
	{
		return Persistence.IdType.OID.isInRange(this.objectId);
	}

	public  boolean isValidConstantReference()
	{
		return Persistence.IdType.CID.isInRange(this.objectId);
	}
}
