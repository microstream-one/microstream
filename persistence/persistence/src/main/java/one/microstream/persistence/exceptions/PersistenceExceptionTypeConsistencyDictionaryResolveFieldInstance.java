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

import java.lang.reflect.Field;

public class PersistenceExceptionTypeConsistencyDictionaryResolveFieldInstance
extends PersistenceExceptionTypeConsistencyDictionaryResolveField
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionTypeConsistencyDictionaryResolveFieldInstance(final Field field)
	{
		this(field, null, null);
	}

	public PersistenceExceptionTypeConsistencyDictionaryResolveFieldInstance(final Field field, final String message)
	{
		this(field, message, null);
	}

	public PersistenceExceptionTypeConsistencyDictionaryResolveFieldInstance(final Field field, final Throwable cause)
	{
		this(field, null, cause);
	}

	public PersistenceExceptionTypeConsistencyDictionaryResolveFieldInstance(
		final Field     field  ,
		final String    message,
		final Throwable cause
	)
	{
		this(field, message, cause, true, true);
	}

	public PersistenceExceptionTypeConsistencyDictionaryResolveFieldInstance(final Field field,
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(field, message, cause, enableSuppression, writableStackTrace);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public String getMessage()
	{
		return "Not a instance (non-static) field: " + this.getField() + "."
			+ (super.getMessage() != null ? " Details: " + super.getMessage() : "")
		;
	}



}
