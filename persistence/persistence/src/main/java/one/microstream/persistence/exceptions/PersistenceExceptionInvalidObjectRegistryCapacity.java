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

public class PersistenceExceptionInvalidObjectRegistryCapacity extends PersistenceException
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static final String messageBody()
	{
		return "Invalid capacity";
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final long invalidCapacity;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionInvalidObjectRegistryCapacity(
		final long invalidCapacity
	)
	{
		this(invalidCapacity, null, null);
	}

	public PersistenceExceptionInvalidObjectRegistryCapacity(
		final long      invalidCapacity,
		final Throwable cause
	)
	{
		this(invalidCapacity, null, cause);
	}

	public PersistenceExceptionInvalidObjectRegistryCapacity(
		final long      invalidCapacity,
		final String    message
	)
	{
		this(invalidCapacity, message, null);
	}

	public PersistenceExceptionInvalidObjectRegistryCapacity(
		final long      invalidCapacity,
		final String    message        ,
		final Throwable cause
	)
	{
		this(invalidCapacity, message, cause, true, true);
	}

	public PersistenceExceptionInvalidObjectRegistryCapacity(
		final long      invalidCapacity   ,
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.invalidCapacity = invalidCapacity;
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	public final long invalidCapacity()
	{
		return this.invalidCapacity;
	}
	
	@Override
	public String assembleDetailString()
	{
		return messageBody() + ": " + this.invalidCapacity + ".";
	}
	
	@Override
	public String toString()
	{
		return this.assembleDetailString();
	}

}
