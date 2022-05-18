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

public class PersistenceExceptionTypeConsistencyDictionaryResolveFieldStatic
extends PersistenceExceptionTypeConsistencyDictionaryResolveField
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionTypeConsistencyDictionaryResolveFieldStatic(final Field field)
	{
		this(field, null, null);
	}

	public PersistenceExceptionTypeConsistencyDictionaryResolveFieldStatic(final Field field, final String message)
	{
		this(field, message, null);
	}

	public PersistenceExceptionTypeConsistencyDictionaryResolveFieldStatic(final Field field, final Throwable cause)
	{
		this(field, null, cause);
	}

	public PersistenceExceptionTypeConsistencyDictionaryResolveFieldStatic(
		final Field     field  ,
		final String    message,
		final Throwable cause
	)
	{
		this(field, message, cause, true, true);
	}

	public PersistenceExceptionTypeConsistencyDictionaryResolveFieldStatic(final Field field,
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
		return "Not a static (non-final) field: " + this.getField() + "."
			+ (super.getMessage() != null ? " Details: " + super.getMessage() : "")
		;
	}



}
