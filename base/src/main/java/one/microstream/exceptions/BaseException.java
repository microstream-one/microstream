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


/**
 * Base class for all exceptions that workarounds some design mistakes in JDK exceptions.
 *
 */
public class BaseException extends RuntimeException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public BaseException()
	{
		super();
	}

	public BaseException(final Throwable cause)
	{
		/*
		 * Because the Throwable(cause) constructor with the hardcoded toString() is not only
		 * inconsistent to all other Throwable constructors.
		 */
		super();
		
		// initialize the cause.
		this.initCause(cause);
	}

	public BaseException(final String message)
	{
		super(message);
	}

	public BaseException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public BaseException(
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public final String message()
	{
		return super.getMessage();
	}

	public String assembleDetailString()
	{
		return null;
	}

	protected String assembleExplicitMessageAddon()
	{
		final String explicitMessage = this.message();
		return explicitMessage != null
			? ": " + explicitMessage
			: ""
		;
	}

	public String assembleOutputString()
	{
		// JDK concept or improved concept based on whether assembleDetailString is overwritten
		final String detailString = this.assembleDetailString();
		return detailString == null
			? super.getMessage()
			: this.assembleDetailString() + this.assembleExplicitMessageAddon()
		;
	}

	/**
	 * Due to bad class design in the JDK's {@link Throwable}, this getter-named methods actually serves as
	 * the output string assembly method.<br>
	 * For the actual message getter, see {@link #message()}.<br>
	 * For the actually executed logic, see {@link #assembleOutputString()}, which is called by this method.<br>
	 *
	 * @return this exception type's generic output string plus an explicit message if present.
	 */
	@Override
	public String getMessage()
	{
		return this.assembleOutputString();
	}
	
}
