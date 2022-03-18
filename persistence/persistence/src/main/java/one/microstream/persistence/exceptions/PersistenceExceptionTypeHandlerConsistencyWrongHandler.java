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

import one.microstream.chars.XChars;
import one.microstream.persistence.types.PersistenceTypeHandler;



public class PersistenceExceptionTypeHandlerConsistencyWrongHandler extends PersistenceExceptionTypeHandlerConsistency
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final Class<?>                    type       ;
	final PersistenceTypeHandler<?, ?> typeHandler;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionTypeHandlerConsistencyWrongHandler(
		final Class<?>                    type       ,
		final PersistenceTypeHandler<?, ?> typeHandler
	)
	{
		this(type, typeHandler, null, null);
	}

	public PersistenceExceptionTypeHandlerConsistencyWrongHandler(
		final Class<?>                    type       ,
		final PersistenceTypeHandler<?, ?> typeHandler,
		final String message
	)
	{
		this(type, typeHandler, message, null);
	}

	public PersistenceExceptionTypeHandlerConsistencyWrongHandler(
		final Class<?>                     type       ,
		final PersistenceTypeHandler<?, ?> typeHandler,
		final Throwable                    cause
	)
	{
		this(type, typeHandler, null, cause);
	}

	public PersistenceExceptionTypeHandlerConsistencyWrongHandler(
		final Class<?>                     type       ,
		final PersistenceTypeHandler<?, ?> typeHandler,
		final String                       message    ,
		final Throwable                    cause
	)
	{
		this(type, typeHandler, message, cause, true, true);
	}

	public PersistenceExceptionTypeHandlerConsistencyWrongHandler(
		final Class<?>                     type              ,
		final PersistenceTypeHandler<?, ?> typeHandler       ,
		final String                       message           ,
		final Throwable                    cause             ,
		final boolean                      enableSuppression ,
		final boolean                      writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.type        = type       ;
		this.typeHandler = typeHandler;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public Class<?> getType()
	{
		return this.type;
	}

	public PersistenceTypeHandler<?, ?> getTypeHandler()
	{
		return this.typeHandler;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public String getMessage()
	{
		return "Wrong handler for type: \"" + this.type + "\": " + XChars.systemString(this.typeHandler)
			+ " with type \"" + this.typeHandler.type() + "\"."
			+ (super.getMessage() != null ? " Details: " + super.getMessage() : "")
		;
	}



}
