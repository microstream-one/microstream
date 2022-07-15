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

public class BinaryPersistenceExceptionStateInstance extends BinaryPersistenceExceptionState
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Object instance;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryPersistenceExceptionStateInstance(final Object instance)
	{
		this(instance, null, null);
	}

	public BinaryPersistenceExceptionStateInstance(final Object instance,
		final String message
	)
	{
		this(instance, message, null);
	}

	public BinaryPersistenceExceptionStateInstance(final Object instance,
		final Throwable cause
	)
	{
		this(instance, null, cause);
	}

	public BinaryPersistenceExceptionStateInstance(final Object instance,
		final String message, final Throwable cause
	)
	{
		this(instance, message, cause, true, true);
	}

	public BinaryPersistenceExceptionStateInstance(final Object instance,
		final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.instance = instance;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public Object getInstance()
	{
		return this.instance;
	}



}
