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

import one.microstream.util.similarity.Similator;

/**
 * Simple implementation of the Levenshtein distance algorithm for calculating Levenshtein distance or a
 * Levenshtein-based string similarity.
 * <p>
 * This class provides three overloaded methods for {@link String}, {@link CharSequence} and {@code char[]}.<br>
 * A constant {@link Similator} function object as well as static convenience methods are provided as well.
 */
public final class Levenshtein
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	public static final double similiarity(final String a, final String b)
	{
		return similarity(a, b, Levenshtein::charDistance);
	}

	public static final double upperCaseSimiliarity(final String a, final String b)
	{
		return similarity(a.toUpperCase(), b.toUpperCase(), Levenshtein::charDistance);
	}

	public static final float charDistance(final char a, final char b)
	{
		return a == b ? 0.0f : 1.0f;
	}

	public static final double substringSimilarity(final String s1, final String s2)
	{
		final char[] c1, c2;
		final double minLen = (c1 = XChars.readChars(s1.toUpperCase())).length
			< (c2 = XChars.readChars(s2.toUpperCase())).length
			? c1.length
			: c2.length
		;
		// (30.07.2011)TODO maybe weight prefix and suffix matches exponentially or so. Maybe with maxLen again.
		return
			(max(
				XChars.commonSubstringLength(c1, c2) - 1, XChars.commonPrefixLength(c1, c2),
				XChars.commonSuffixLength(c1, c2)
			) / minLen
			 + Levenshtein.similarity(c1, c2)
			) / 2.0D
		;
	}



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	static final int max(final int a, final int b, final int c)
	{
		return a >= b ? a >= c ? a : c : b >= c ? b : c;
	}

	static final float min(final float f1, final float f2, final float f3)
	{
		return f1 < f2 ? f1 < f3 ? f1 : f3 : f2 < f3 ? f2 : f3;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	public static final float distance(final String a, final String b, final _charDistance costFunction)
	{
		final int lenA, lenB;
		if((lenA = a.length()) == 0)
		{
			return b.length();
		}
		if((lenB = b.length()) == 0)
		{
			return lenA;
		}

		final float[][] matrix = new float[lenA + 1][lenB + 1];
		for(int ia = 0; ia <= lenA; ia++)
		{
			matrix[ia][0] = ia;
		}
		for(int ib = 0; ib <= lenB; ib++)
		{
			matrix[0][ib] = ib;
		}
		for(int ia = 0; ia < lenA; ia++)
		{
			for(int ib = 0; ib < lenB; ib++)
			{
				matrix[ia + 1][ib + 1] = min(
					matrix[ia    ][ib + 1] + 1.0f,
					matrix[ia + 1][ib    ] + 1.0f,
					matrix[ia    ][ib    ] + costFunction.distance(a.charAt(ia), b.charAt(ib))
				);
			}
		}
		return matrix[lenA][lenB];
	}

	public static final float distance(final CharSequence a, final CharSequence b, final _charDistance costFunction)
	{
		final int lenA, lenB;
		if((lenA = a.length()) == 0)
		{
			return b.length();
		}
		if((lenB = b.length()) == 0)
		{
			return lenA;
		}

		final float[][] matrix = new float[lenA + 1][lenB + 1];
		for(int ia = 0; ia <= lenA; ia++)
		{
			matrix[ia][0] = ia;
		}
		for(int ib = 0; ib <= lenB; ib++)
		{
			matrix[0][ib] = ib;
		}
		for(int ia = 0; ia < lenA; ia++)
		{
			for(int ib = 0; ib < lenB; ib++)
			{
				matrix[ia + 1][ib + 1] = min(
					matrix[ia    ][ib + 1] + 1.0f,
					matrix[ia + 1][ib    ] + 1.0f,
					matrix[ia    ][ib    ] + costFunction.distance(a.charAt(ia), b.charAt(ib))
				);
			}
		}
		return matrix[lenA][lenB];
	}

	public static final float distance(final char[] a, final char[] b, final _charDistance costFunction)
	{
		final int lenA, lenB;
		if((lenA = a.length) == 0)
		{
			return b.length;
		}
		if((lenB = b.length) == 0)
		{
			return lenA;
		}

		final float[][] matrix = new float[lenA + 1][lenB + 1];
		for(int ia = 0; ia <= lenA; ia++)
		{
			matrix[ia][0] = ia;
		}
		for(int ib = 0; ib <= lenB; ib++)
		{
			matrix[0][ib] = ib;
		}
		for(int ia = 0; ia < lenA; ia++)
		{
			for(int ib = 0; ib < lenB; ib++)
			{
				matrix[ia + 1][ib + 1] = min(
					matrix[ia    ][ib + 1] + 1.0f,
					matrix[ia + 1][ib    ] + 1.0f,
					matrix[ia    ][ib    ] + costFunction.distance(a[ia], b[ib])
				);
			}
		}
		return matrix[lenA][lenB];
	}


	public static final float similarity(final String a, final String b, final _charDistance costFunction)
	{
		if(a.isEmpty() && b.isEmpty())
		{
			return 1.0f;
		}
		return 1.0f - distance(a, b, costFunction) / (a.length() < b.length() ? b.length() : a.length());
	}

	public static final float similarity(final CharSequence a, final CharSequence b, final _charDistance costFunction)
	{
		if(a.length() == 0 && b.length() == 0)
		{
			return 1.0f;
		}
		return 1.0f - distance(a, b, costFunction) / (a.length() < b.length() ? b.length() : a.length());
	}

	public static final float similarity(final char[] a, final char[] b, final _charDistance costFunction)
	{
		if(a.length == 0 && b.length == 0)
		{
			return 1.0f;
		}
		return 1.0f - distance(a, b, costFunction) / (a.length < b.length ? b.length : a.length);
	}

	public static final float similarity(final String a, final String b)
	{
		return similarity(a, b, Levenshtein::charDistance);
	}

	public static final float similarity(final CharSequence a, final CharSequence b)
	{
		return similarity(a, b, Levenshtein::charDistance);
	}

	public static final float similarity(final char[] a, final char[] b)
	{
		return similarity(a, b, Levenshtein::charDistance);
	}

	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException when called
	 */
	private Levenshtein()
	{
		// static only
		throw new UnsupportedOperationException();
	}

}
