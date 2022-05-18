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

public class PersistenceExceptionTypeConsistencyDictionaryResolveFieldType
extends PersistenceExceptionTypeConsistencyDictionaryResolveField
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Class<?> dictionaryFieldType;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionTypeConsistencyDictionaryResolveFieldType(
		final Field    field,
		final Class<?> dictionaryFieldType
	)
	{
		this(field, dictionaryFieldType, null, null);
	}

	public PersistenceExceptionTypeConsistencyDictionaryResolveFieldType(
		final Field    field,
		final Class<?> dictionaryFieldType,
		final String message
	)
	{
		this(field, dictionaryFieldType, message, null);
	}

	public PersistenceExceptionTypeConsistencyDictionaryResolveFieldType(
		final Field    field,
		final Class<?> dictionaryFieldType,
		final Throwable cause
	)
	{
		this(field, dictionaryFieldType, null, cause);
	}

	public PersistenceExceptionTypeConsistencyDictionaryResolveFieldType(
		final Field    field,
		final Class<?> dictionaryFieldType,
		final String message, final Throwable cause
	)
	{
		this(field, dictionaryFieldType, message, cause, true, true);
	}

	public PersistenceExceptionTypeConsistencyDictionaryResolveFieldType(
		final Field     field,
		final Class<?>  dictionaryFieldType,
		final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace
	)
	{
		super(field, message, cause, enableSuppression, writableStackTrace);
		this.dictionaryFieldType = dictionaryFieldType;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public Class<?> getDictionaryFieldType()
	{
		return this.dictionaryFieldType;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public String getMessage()
	{
		return "Error on validation of field " + this.getField()
			+ ": when matching dictionary field type (" + this.dictionaryFieldType
			+ ") with type of actual field (" + this.getField().getType() + ")."
			+ (super.getMessage() != null ? " Details: " + super.getMessage() : "")
		;
	}



}
