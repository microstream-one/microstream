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

import one.microstream.chars.XChars;

public class BinaryPersistenceExceptionStateArrayLength extends BinaryPersistenceExceptionStateArray
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Object[] actualArray ;
	private final int      passedLength;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryPersistenceExceptionStateArrayLength(
		final Object[] actualArray ,
		final int      passedLength
	)
	{
		this(actualArray, passedLength, null, null);
	}

	public BinaryPersistenceExceptionStateArrayLength(
		final Object[] actualArray ,
		final int      passedLength,
		final String message
	)
	{
		this(actualArray, passedLength, message, null);
	}

	public BinaryPersistenceExceptionStateArrayLength(
		final Object[] actualArray ,
		final int      passedLength,
		final Throwable cause
	)
	{
		this(actualArray, passedLength, null, cause);
	}

	public BinaryPersistenceExceptionStateArrayLength(
		final Object[] actualArray ,
		final int      passedLength,
		final String message, final Throwable cause
	)
	{
		this(actualArray, passedLength, message, cause, true, true);
	}

	public BinaryPersistenceExceptionStateArrayLength(
		final Object[] actualArray ,
		final int      passedLength,
		final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.actualArray  = actualArray ;
		this.passedLength = passedLength;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public Object[] getActualArray()
	{
		return this.actualArray;
	}

	public int getPassedLength()
	{
		return this.passedLength;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public String getMessage()
	{
		return "Array length mismatch for array " + XChars.systemString(this.actualArray) + ": actual length = "
			+ this.actualArray.length + ", passed length = " + this.passedLength + "."
			+ (super.getMessage() != null ? " Details: " + super.getMessage() : "")
		;
	}



}
