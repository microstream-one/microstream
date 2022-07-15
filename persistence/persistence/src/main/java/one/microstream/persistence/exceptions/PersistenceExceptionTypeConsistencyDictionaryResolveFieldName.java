
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

public class PersistenceExceptionTypeConsistencyDictionaryResolveFieldName
extends PersistenceExceptionTypeConsistencyDictionary
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Class<?> declaringType;
	private final String   fieldName    ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionTypeConsistencyDictionaryResolveFieldName(
		final Class<?> declaringType,
		final String fieldName
	)
	{
		this(declaringType, fieldName, null, null);
	}

	public PersistenceExceptionTypeConsistencyDictionaryResolveFieldName(
		final Class<?> declaringType,
		final String fieldName,
		final String message
	)
	{
		this(declaringType, fieldName, message, null);
	}

	public PersistenceExceptionTypeConsistencyDictionaryResolveFieldName(
		final Class<?> declaringType,
		final String fieldName,
		final Throwable cause
	)
	{
		this(declaringType, fieldName, null, cause);
	}

	public PersistenceExceptionTypeConsistencyDictionaryResolveFieldName(
		final Class<?> declaringType,
		final String fieldName,
		final String message, final Throwable cause
	)
	{
		this(declaringType, fieldName, message, cause, true, true);
	}

	public PersistenceExceptionTypeConsistencyDictionaryResolveFieldName(
		final Class<?> declaringType,
		final String fieldName,
		final String message,
		final Throwable cause,
		final boolean enableSuppression,
		final boolean writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.declaringType = declaringType;
		this.fieldName     = fieldName    ;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public Class<?> getDeclaringType()
	{
		return this.declaringType;
	}

	public String getTypeName()
	{
		return this.fieldName;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public String getMessage()
	{
		return "Unresolvable dictionary field \"" + this.declaringType.getName() + "#" + this.fieldName + "\"."
			+ (super.getMessage() != null ? " Details: " + super.getMessage() : "")
		;
	}



}
