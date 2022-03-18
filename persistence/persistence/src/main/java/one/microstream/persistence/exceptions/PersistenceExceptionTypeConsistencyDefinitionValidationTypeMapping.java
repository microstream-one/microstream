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

public class PersistenceExceptionTypeConsistencyDefinitionValidationTypeMapping
extends PersistenceExceptionTypeConsistencyDefinitionValidation
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final long     typeId;
	private final Class<?> type  ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionTypeConsistencyDefinitionValidationTypeMapping(
		final long     typeId,
		final Class<?> type
	)
	{
		this(typeId, type, null, null);
	}

	public PersistenceExceptionTypeConsistencyDefinitionValidationTypeMapping(
		final long     typeId,
		final Class<?> type  ,
		final String message
	)
	{
		this(typeId, type, message, null);
	}

	public PersistenceExceptionTypeConsistencyDefinitionValidationTypeMapping(
		final long     typeId,
		final Class<?> type  ,
		final Throwable cause
	)
	{
		this(typeId, type, null, cause);
	}

	public PersistenceExceptionTypeConsistencyDefinitionValidationTypeMapping(
		final long     typeId,
		final Class<?> type  ,
		final String message, final Throwable cause
	)
	{
		this(typeId, type, message, cause, true, true);
	}

	public PersistenceExceptionTypeConsistencyDefinitionValidationTypeMapping(
		final long     typeId,
		final Class<?> type  ,
		final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.typeId = typeId;
		this.type   = type  ;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public Class<?> getType()
	{
		return this.type;
	}

	public long getTypeId()
	{
		return this.typeId;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public String getMessage()
	{
		return "Invalid type mapping: " + this.typeId + " " + this.type + "."
			+ (super.getMessage() != null ? " Details: " + super.getMessage() : "")
		;
	}



}
