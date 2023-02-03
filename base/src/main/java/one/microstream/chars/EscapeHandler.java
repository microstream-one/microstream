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

import one.microstream.typing.Stateless;


public interface EscapeHandler
{
	public void handleEscapedChar(char escapedChar, VarString literalBuilder);

	public boolean needsEscaping(char chr);

	/**
	 * Transforms the given character on demand. Following escaped characters are transformed:
	 * <ul>
	 * <li>'\t' -&gt; 't'</li>
	 * <li>'\b' -&gt; 'b'</li>
	 * <li>'\n' -&gt; 'n'</li>
	 * <li>'\r' -&gt; 'r'</li>
	 * <li>'\f' -&gt; 'f'</li>
	 * </ul>
	 * 
	 * If no transformation is needed, the same character value is returned.
	 * 
	 * @param chr the character to uneescape
	 * @return the unescaped value
	 */
	public char transformEscapedChar(char chr);

	public char unescape(char chr);

	public static char parseCharacter(final String c, final char escapeCharacter)
	{
		if(c == null || c.isEmpty())
		{
			throw new IllegalArgumentException("String is empty");
		}

		if(c.charAt(0) == escapeCharacter)
		{
			if(c.length() > 2)
			{
				throw new IllegalArgumentException("String consists of more than one character");
			}
			return Default.internalUnescape(c.charAt(1));
		}
		else if(c.length() > 1)
		{
			throw new IllegalArgumentException("String consists of more than one character");
		}
		else
		{
			return c.charAt(0);
		}
	}



	public final class Default implements EscapeHandler, Stateless
	{
		static char internalUnescape(final char chr)
		{
			/* note:
			 * The left side are (arbitrary) literal escaping symbols,
			 * the right side are java syntax control character symbols.
			 */
			switch(chr)
			{
				case 't': return '\t';
				case 'b': return '\b';
				case 'n': return '\n';
				case 'r': return '\r';
				case 'f': return '\f';
				default : return chr ; // unmapped character, return directly
			}
		}

		@Override
		public final char unescape(final char chr)
		{
			return internalUnescape(chr);
		}

		@Override
		public final void handleEscapedChar(final char escapedChar, final VarString literalBuilder)
		{
			literalBuilder.add(internalUnescape(escapedChar));
		}

		@Override
		public final boolean needsEscaping(final char chr)
		{
			switch(chr)
			{
				case '\b':
				case '\f':
				case '\n':
				case '\r':
				case '\t': return true;
				default  : return false;
			}
		}

		@Override
		public final char transformEscapedChar(final char chr)
		{
			switch(chr)
			{
				case '\t': return 't';
				case '\b': return 'b';
				case '\n': return 'n';
				case '\r': return 'r';
				case '\f': return 'f';
				default  : return chr; // unmapped character, return directly
			}
		}

	}

}
