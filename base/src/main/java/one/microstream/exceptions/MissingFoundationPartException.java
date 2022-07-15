package one.microstream.exceptions;

/*-
 * #%L
 * microstream-base
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


public class MissingFoundationPartException extends RuntimeException
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Class<?> missingAssemblyPartType;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public MissingFoundationPartException(
		final Class<?> missingSssemblyPartType
	)
	{
		this(missingSssemblyPartType, null, null);
	}

	public MissingFoundationPartException(
		final Class<?> missingSssemblyPartType,
		final String message
	)
	{
		this(missingSssemblyPartType, message, null);
	}

	public MissingFoundationPartException(
		final Class<?> missingSssemblyPartType,
		final Throwable cause
	)
	{
		this(missingSssemblyPartType, null, cause);
	}

	public MissingFoundationPartException(
		final Class<?> missingSssemblyPartType,
		final String message, final Throwable cause
	)
	{
		this(missingSssemblyPartType, message, cause, true, true);
	}

	public MissingFoundationPartException(
		final Class<?> missingSssemblyPartType,
		final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.missingAssemblyPartType = missingSssemblyPartType;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public Class<?> getMissingSssemblyPartType()
	{
		return this.missingAssemblyPartType;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////
	
	public String assembleDetailString()
	{
		return this.missingAssemblyPartType != null
			? "Missing assembly part of type " + this.missingAssemblyPartType.toString() + ". "
			: ""
		;
	}

	protected String assembleExplicitMessageAddon()
	{
		final String explicitMessage = super.getMessage();
		return explicitMessage == null ? "" : " (" + explicitMessage + ")";
	}

	public String assembleOutputString()
	{
		return this.assembleDetailString() + this.assembleExplicitMessageAddon();
	}
	
	/**
	 * Returns an assembled output String due to bad method design in {@link Throwable}.
	 *
	 * @return this exception type's generic string plus an explicit message if present.
	 */
	@Override
	public String getMessage() // intentionally not final to enable subclasses to change the behavior again
	{
		return this.assembleOutputString();
	}

}
