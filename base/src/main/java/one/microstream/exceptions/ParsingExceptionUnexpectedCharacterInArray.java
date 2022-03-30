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

public class ParsingExceptionUnexpectedCharacterInArray extends ParsingExceptionUnexpectedCharacter
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final char[] array;
	private final int    index;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ParsingExceptionUnexpectedCharacterInArray(
		final char[] array               ,
		final int    index               ,
		final char   expectedCharacter   ,
		final char   encounteredCharacter
	)
	{
		super(expectedCharacter, encounteredCharacter);
		this.array = array;
		this.index = index;
	}

	public ParsingExceptionUnexpectedCharacterInArray(
		final char[]    array               ,
		final int       index               ,
		final char      expectedCharacter   ,
		final char      encounteredCharacter,
		final Throwable cause
	)
	{
		super(expectedCharacter, encounteredCharacter, cause);
		this.array = array;
		this.index = index;
	}

	public ParsingExceptionUnexpectedCharacterInArray(
		final char[] array               ,
		final int    index               ,
		final char   expectedCharacter   ,
		final char   encounteredCharacter,
		final String message
	)
	{
		super(expectedCharacter, encounteredCharacter, message);
		this.array = array;
		this.index = index;
	}

	public ParsingExceptionUnexpectedCharacterInArray(
		final char[]    array               ,
		final int       index               ,
		final char      expectedCharacter   ,
		final char      encounteredCharacter,
		final String    message             ,
		final Throwable cause
	)
	{
		super(expectedCharacter, encounteredCharacter, message, cause);
		this.array = array;
		this.index = index;
	}

	public ParsingExceptionUnexpectedCharacterInArray(
		final char[]    array               ,
		final int       index               ,
		final char      expectedCharacter   ,
		final char      encounteredCharacter,
		final String    message             ,
		final Throwable cause               ,
		final boolean   enableSuppression   ,
		final boolean   writableStackTrace
	)
	{
		super(expectedCharacter, encounteredCharacter, message, cause, enableSuppression, writableStackTrace);
		this.array = array;
		this.index = index;
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public final char[] array()
	{
		return this.array;
	}
	
	public final int index()
	{
		return this.index;
	}
	
	@Override
	public String assembleDetailString()
	{
		return "Problem at index " + this.index + ": " + super.assembleDetailString();
	}
	
}
