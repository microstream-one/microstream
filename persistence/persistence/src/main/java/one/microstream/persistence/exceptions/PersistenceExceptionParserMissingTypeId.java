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

public class PersistenceExceptionParserMissingTypeId extends PersistenceExceptionParser
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionParserMissingTypeId(
		final int index
	)
	{
		this(index, null, null);
	}

	public PersistenceExceptionParserMissingTypeId(
		final int index,
		final String message
	)
	{
		this(index, message, null);
	}

	public PersistenceExceptionParserMissingTypeId(
		final int index,
		final Throwable cause
	)
	{
		this(index, null, cause);
	}

	public PersistenceExceptionParserMissingTypeId(
		final int index,
		final String message, final Throwable cause
	)
	{
		this(index, message, cause, true, true);
	}

	public PersistenceExceptionParserMissingTypeId(
		final int index,
		final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace
	)
	{
		super(index, message, cause, enableSuppression, writableStackTrace);
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	@Override
	public String getMessage()
	{
		return "Missing type id at index " + this.getIndex() + "."
			+ (super.getMessage() != null ? " Details: " + super.getMessage() : "")
		;
	}



}
