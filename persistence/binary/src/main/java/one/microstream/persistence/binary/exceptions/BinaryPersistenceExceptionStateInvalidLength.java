package one.microstream.persistence.binary.exceptions;

/*-
 * #%L
 * microstream-persistence-binary
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


public class BinaryPersistenceExceptionStateInvalidLength extends BinaryPersistenceExceptionState
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final long address  ;
	private final long length   ;
	private final long typeId   ;
	private final long objectOid;




	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryPersistenceExceptionStateInvalidLength(
		final long address,
		final long length ,
		final long typeId ,
		final long objectOid
	)
	{
		this(address, length, typeId, objectOid, null, null);
	}

	public BinaryPersistenceExceptionStateInvalidLength(
		final long address  ,
		final long length   ,
		final long typeId   ,
		final long objectOid,
		final String message
	)
	{
		this(address, length, typeId, objectOid, message, null);
	}

	public BinaryPersistenceExceptionStateInvalidLength(
		final long address  ,
		final long length   ,
		final long typeId   ,
		final long objectOid,
		final Throwable cause
	)
	{
		this(address, length, typeId, objectOid, null, cause);
	}

	public BinaryPersistenceExceptionStateInvalidLength(
		final long address  ,
		final long length   ,
		final long typeId   ,
		final long objectOid,
		final String message, final Throwable cause
	)
	{
		this(address, length, typeId, objectOid, message, cause, true, true);
	}

	public BinaryPersistenceExceptionStateInvalidLength(
		final long address  ,
		final long length   ,
		final long typeId   ,
		final long objectOid,
		final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.address   = address  ;
		this.length    = length   ;
		this.typeId    = typeId   ;
		this.objectOid = objectOid;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public long getLength()
	{
		return this.length;
	}

	public long getTypeId()
	{
		return this.typeId;
	}

	public long getAddress()
	{
		return this.address;
	}

	public long getObjectOid()
	{
		return this.objectOid;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public String getMessage()
	{
		return "Invalid length: " + this.length + "."
			+ "TypeId = " + this.typeId
			+ ", objectId = " + this.objectOid
			+ ", address = " + this.address
			+ (super.getMessage() != null ? " Details: " + super.getMessage() : "")
		;
	}



}
