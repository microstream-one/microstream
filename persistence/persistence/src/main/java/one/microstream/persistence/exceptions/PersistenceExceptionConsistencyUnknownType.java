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


public class PersistenceExceptionConsistencyUnknownType extends PersistenceExceptionConsistency
{
	///////////////////////////////////////////////////////////////////////////
	// instance types //
	////////////////////

	final Class<?> type;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionConsistencyUnknownType(final Class<?> type)
	{
		this(type, null, null);
	}

	public PersistenceExceptionConsistencyUnknownType(final Class<?> type, final String message)
	{
		this(type, message, null);
	}

	public PersistenceExceptionConsistencyUnknownType(final Class<?> type, final Throwable cause)
	{
		this(type, null, cause);
	}

	public PersistenceExceptionConsistencyUnknownType(final Class<?> type, final String message, final Throwable cause)
	{
		this(type, message, cause, true, true);
	}

	public PersistenceExceptionConsistencyUnknownType(
		final Class<?>  type              ,
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.type = type;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public Class<?> getType()
	{
		return this.type;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public String getMessage()
	{
		return "Unknown type: \"" + this.type + "\"."
			+ (super.getMessage() != null ? " Details: " + super.getMessage() : "")
		;
	}

}
