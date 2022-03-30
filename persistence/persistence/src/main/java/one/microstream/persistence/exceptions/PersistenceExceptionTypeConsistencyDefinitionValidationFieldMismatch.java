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

public class PersistenceExceptionTypeConsistencyDefinitionValidationFieldMismatch
extends PersistenceExceptionTypeConsistencyDefinitionValidation
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Field actualField ;
	private final Field definedField;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionTypeConsistencyDefinitionValidationFieldMismatch(
		final Field actualField ,
		final Field definedField
	)
	{
		this(actualField, definedField, null, null);
	}

	public PersistenceExceptionTypeConsistencyDefinitionValidationFieldMismatch(
		final Field actualField ,
		final Field definedField,
		final String message
	)
	{
		this(actualField, definedField, message, null);
	}

	public PersistenceExceptionTypeConsistencyDefinitionValidationFieldMismatch(
		final Field actualField ,
		final Field definedField,
		final Throwable cause
	)
	{
		this(actualField, definedField, null, cause);
	}

	public PersistenceExceptionTypeConsistencyDefinitionValidationFieldMismatch(
		final Field actualField ,
		final Field definedField,
		final String message, final Throwable cause
	)
	{
		this(actualField, definedField, message, cause, true, true);
	}

	public PersistenceExceptionTypeConsistencyDefinitionValidationFieldMismatch(
		final Field actualField ,
		final Field definedField,
		final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.actualField  = actualField ;
		this.definedField = definedField;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public Object getActualType()
	{
		return this.actualField;
	}

	public Object getDefinedType()
	{
		return this.definedField;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public String getMessage()
	{
		return "Field mismatch: actual field = \"" + this.actualField
			+ "\", defined field = \"" + this.definedField + "\"."
			+ (super.getMessage() != null ? " Details: " + super.getMessage() : "")
		;
	}



}
