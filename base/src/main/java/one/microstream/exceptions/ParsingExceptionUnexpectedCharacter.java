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

public class ParsingExceptionUnexpectedCharacter extends ParsingException
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final char expectedCharacter   ;
	private final char encounteredCharacter;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ParsingExceptionUnexpectedCharacter(
		final char expectedCharacter   ,
		final char encounteredCharacter
	)
	{
		super();
		this.expectedCharacter    = expectedCharacter   ;
		this.encounteredCharacter = encounteredCharacter;
	}

	public ParsingExceptionUnexpectedCharacter(
		final char      expectedCharacter   ,
		final char      encounteredCharacter,
		final Throwable cause
	)
	{
		super(cause);
		this.expectedCharacter    = expectedCharacter   ;
		this.encounteredCharacter = encounteredCharacter;
	}

	public ParsingExceptionUnexpectedCharacter(
		final char   expectedCharacter   ,
		final char   encounteredCharacter,
		final String message
	)
	{
		super(message);
		this.expectedCharacter    = expectedCharacter   ;
		this.encounteredCharacter = encounteredCharacter;
	}

	public ParsingExceptionUnexpectedCharacter(
		final char      expectedCharacter   ,
		final char      encounteredCharacter,
		final String    message             ,
		final Throwable cause
	)
	{
		super(message, cause);
		this.expectedCharacter    = expectedCharacter   ;
		this.encounteredCharacter = encounteredCharacter;
	}

	public ParsingExceptionUnexpectedCharacter(
		final char      expectedCharacter   ,
		final char      encounteredCharacter,
		final String    message             ,
		final Throwable cause               ,
		final boolean   enableSuppression   ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.expectedCharacter    = expectedCharacter   ;
		this.encounteredCharacter = encounteredCharacter;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public final char expectedCharacter()
	{
		return this.expectedCharacter;
	}
	
	public final char encounteredCharacter()
	{
		return this.encounteredCharacter;
	}
	
	@Override
	public String assembleDetailString()
	{
		return "Encountered character '"
			+ this.encounteredCharacter
			+ "' is not the expected character '"
			+ this.expectedCharacter
			+ "'."
		;
	}
	
}
