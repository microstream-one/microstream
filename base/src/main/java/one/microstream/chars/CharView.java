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

public final class CharView implements CharSequence
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final char[] data;
	final int offset;
	final int length;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public CharView(final char[] data, final int offset, final int length)
	{
		super();
		this.data   = data  ;
		this.offset = offset;
		this.length = length;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	public final void validateIndex(final int index)
	{
		if(index < 0 || index >= this.length)
		{
			throw new StringIndexOutOfBoundsException(index);
		}
	}

	public final void validateRange(final int offset, final int length)
	{
		this.validateIndex(offset);
		this.validateIndex(offset + length - 1);
	}

	// (30.07.2013 TM)TODO: reading methods like in VarString



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final int length()
	{
		return this.length;
	}

	@Override
	public final char charAt(final int index)
	{
		this.validateIndex(index);
		return this.data[this.offset + index];
	}

	@Override
	public final CharSequence subSequence(final int start, final int end)
	{
		this.validateIndex(start);
		this.validateIndex(end);
		if(start > end)
		{
			throw new IllegalArgumentException();
		}
		return new CharView(this.data, this.offset + start, end - start);
	}

	@Override
	public final String toString()
	{
		return new String(this.data, this.offset, this.length);
	}

}
