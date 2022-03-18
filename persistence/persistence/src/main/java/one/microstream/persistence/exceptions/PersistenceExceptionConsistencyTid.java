package one.microstream.persistence.exceptions;

/*-
 * #%L
 * microstream-persistence
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

public class PersistenceExceptionConsistencyTid extends PersistenceExceptionConsistency
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final Object reference   ;
	final long   objectId    ;
	final long   actualTypeId;
	final long   passedTypeId;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionConsistencyTid(
		final long   objectId ,
		final long   actualTypeId,
		final long   passedTypeId,
		final Object reference
	)
	{
		super();
		this.reference    = reference   ;
		this.objectId     = objectId    ;
		this.actualTypeId = actualTypeId;
		this.passedTypeId = passedTypeId;
	}

	

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	public Object reference()
	{
		return this.reference;
	}

	public long objectId()
	{
		return this.objectId;
	}

	public long actualTypeId()
	{
		return this.actualTypeId;
	}

	public long passedTypeId()
	{
		return this.passedTypeId;
	}

	@Override
	public String assembleDetailString()
	{
		return "ObjectId = " + this.objectId()
			+ ", actual TypeId = " + this.actualTypeId()
			+ ", passed TypeId = " + this.passedTypeId()
		;
	}

}
