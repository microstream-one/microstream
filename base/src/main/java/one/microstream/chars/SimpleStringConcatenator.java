package one.microstream.chars;

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

public class SimpleStringConcatenator
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	private static int countTotalChars(final String[] parts)
	{
		int totalCharCount = 0;
		for(final String s : parts)
		{
			if(s == null)
			{
				continue;
			}
			totalCharCount += s.length();
		}
		return totalCharCount;
	}

	public static StringBuilder assemble(final StringBuilder sb, final String[] parts, final Object... values)
	{
		int i = 0;

		//merge parts and values as long as there are enough values
		while(i < parts.length && i < values.length)
		{
			sb.append(parts[i]).append(values[i++]);
		}

		//if there are too few values, add up the remaining parts (does nothing otherwise)
		while(i < parts.length)
		{
			sb.append(parts[i++]);
		}

		return sb;
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private       int      reservedCharCount;
	private final String[] parts            ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public SimpleStringConcatenator(final String[] parts)
	{
		this(parts, parts.length);
	}

	public SimpleStringConcatenator(final String[] parts, final int reservedValueCharacterCount)
	{
		super();
		this.reservedCharCount = countTotalChars(parts)
			+ reservedValueCharacterCount < 0 ? 0 : reservedValueCharacterCount
		;
		this.parts = parts;
	}

	public SimpleStringConcatenator(final String[] parts, final float reservedValueCharacterFactor)
	{
		this(
			parts,
			(int)(countTotalChars(parts) * (reservedValueCharacterFactor < 1.0f ? 1.0f : reservedValueCharacterFactor))
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	public int getReservedCharCount()
	{
		return this.reservedCharCount;
	}

	public String[] getParts()
	{
		return this.parts;
	}

	public void setReservedCharCount(final int reservedCharCount)
	{
		this.reservedCharCount = reservedCharCount;
	}

	public StringBuilder assemble(final Object... values)
	{
		return assemble(new StringBuilder(this.reservedCharCount), this.parts, values);
	}

	public StringBuilder assemble(final StringBuilder sb, final Object... values)
	{
		return assemble(sb, this.parts, values);
	}

}
