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



public class PersistenceExceptionTypeHandlerConsistencyConflictedType extends PersistenceExceptionTypeHandlerConsistency
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final Class<?>                    type             ;
	final PersistenceTypeHandler<?, ?> actualTypeHandler;
	final PersistenceTypeHandler<?, ?> passedTypeHandler;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionTypeHandlerConsistencyConflictedType(
		final Class<?>                    type             ,
		final PersistenceTypeHandler<?, ?> actualTypeHandler,
		final PersistenceTypeHandler<?, ?> passedTypeHandler
	)
	{
		this(type, actualTypeHandler, passedTypeHandler, null, null);
	}

	public PersistenceExceptionTypeHandlerConsistencyConflictedType(
		final Class<?>                     type             ,
		final PersistenceTypeHandler<?, ?> actualTypeHandler,
		final PersistenceTypeHandler<?, ?> passedTypeHandler,
		final String                       message
	)
	{
		this(type, actualTypeHandler, passedTypeHandler, message, null);
	}

	public PersistenceExceptionTypeHandlerConsistencyConflictedType(
		final Class<?>                     type              ,
		final PersistenceTypeHandler<?, ?> actualTypeHandler,
		final PersistenceTypeHandler<?, ?> passedTypeHandler,
		final Throwable                    cause
	)
	{
		this(type, actualTypeHandler, passedTypeHandler, null, cause);
	}

	public PersistenceExceptionTypeHandlerConsistencyConflictedType(
		final Class<?>                     type             ,
		final PersistenceTypeHandler<?, ?> actualTypeHandler,
		final PersistenceTypeHandler<?, ?> passedTypeHandler,
		final String                       message          ,
		final Throwable                    cause
	)
	{
		this(type, actualTypeHandler, passedTypeHandler, message, cause, true, true);
	}

	public PersistenceExceptionTypeHandlerConsistencyConflictedType(
		final Class<?>                     type              ,
		final PersistenceTypeHandler<?, ?> actualTypeHandler ,
		final PersistenceTypeHandler<?, ?> passedTypeHandler ,
		final String                       message           ,
		final Throwable                    cause             ,
		final boolean                      enableSuppression ,
		final boolean                      writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.type              = type             ;
		this.actualTypeHandler = actualTypeHandler;
		this.passedTypeHandler = passedTypeHandler;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public Class<?> getType()
	{
		return this.type;
	}

	public PersistenceTypeHandler<?, ?> getActualTypeHandler()
	{
		return this.actualTypeHandler;
	}

	public PersistenceTypeHandler<?, ?> getPassedTypeHandler()
	{
		return this.passedTypeHandler;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public String getMessage()
	{
		return "Type \"" + this.type + "\" is already associated to type handler "
			+ XChars.systemString(this.actualTypeHandler)
			+ ", cannot be associated to type handler \"" + XChars.systemString(this.passedTypeHandler) + "\" as well."
			+ (super.getMessage() != null ? " Details: " + super.getMessage() : "")
		;
	}



}
