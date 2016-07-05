/*
 * Copyright (c) 2008-2010, Thomas Muenz
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.jadoth.util.chars;

/**
 *
 * @author Thomas Muenz
 */
public class SimpleStringConcatenator
{
	///////////////////////////////////////////////////////////////////////////
	//  static methods   //
	/////////////////////

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
	// instance fields  //
	/////////////////////

	private       int      reservedCharCount;
	private final String[] parts            ;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

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
