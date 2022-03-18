
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


public class PersistenceExceptionTypeConsistencyDefinitionResolveTypeName
extends PersistenceExceptionTypeConsistencyDictionary
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static final String messageBody()
	{
		return "Unresolvable dictionary type";
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance typeNames //
	///////////////////////

	private final String typeName;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionTypeConsistencyDefinitionResolveTypeName(final String typeName)
	{
		this(typeName, null, null);
	}

	public PersistenceExceptionTypeConsistencyDefinitionResolveTypeName(final String typeName, final String message)
	{
		this(typeName, message, null);
	}

	public PersistenceExceptionTypeConsistencyDefinitionResolveTypeName(final String typeName, final Throwable cause)
	{
		this(typeName, null, cause);
	}

	public PersistenceExceptionTypeConsistencyDefinitionResolveTypeName(
		final String    typeName,
		final String    message ,
		final Throwable cause
	)
	{
		this(typeName, message, cause, true, true);
	}

	public PersistenceExceptionTypeConsistencyDefinitionResolveTypeName(
		final String    typeName          ,
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.typeName = typeName;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public String typeName()
	{
		return this.typeName;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////
	
	@Override
	public String assembleDetailString()
	{
		return messageBody() + ": \"" + this.typeName() + "\"";
	}

}
