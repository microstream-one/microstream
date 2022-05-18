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

import one.microstream.collections.XArrays;



public interface _charArrayRange
{
	public char[] array();

	public int start();

	public int bound();

	
	

	public static Default New(final String value)
	{
		return New(XChars.readChars(value));
	}

	public static Default New(final char[] array)
	{
		return new Default(array, 0, array.length);
	}

	public static Default New(final char[] array, final int offset)
	{
		return new Default(
			array,
			XArrays.validateArrayIndex(array.length, offset),
			array.length
		);
	}

	public static Default New(final char[] array, final int offset, final int bound)
	{
		return new Default(
			array,
			XArrays.validateArrayIndex(array.length, offset),
			XArrays.validateArrayIndex(array.length, bound - 1) + 1 // nasty
		);
	}



	public final class Default implements _charArrayRange
	{
		////////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final char[] array;
		final int    start;
		final int    bound;



		////////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final char[] array, final int start, final int bound)
		{
			super();
			this.array = array;
			this.start = start;
			this.bound = bound;
		}



		////////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final char[] array()
		{
			return this.array;
		}

		@Override
		public final int start()
		{
			return this.start;
		}

		@Override
		public final int bound()
		{
			return this.bound;
		}

	}



	public final class Mutable implements _charArrayRange
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////

		public static final Mutable New(final char[] array)
		{
			return new Mutable(array, 0, array.length);
		}

		public static final Mutable New(final char[] array, final int offset)
		{
			return new Mutable(
				array,
				XArrays.validateArrayIndex(array.length, offset),
				array.length
			);
		}

		public static final Mutable New(final char[] array, final int offset, final int bound)
		{
			return new Mutable(
				array,
				XArrays.validateArrayIndex(array.length, offset),
				XArrays.validateArrayIndex(array.length, bound)
			);
		}



		////////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final char[] array;
		int   start, bound;



		////////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		private Mutable(final char[] array, final int start, final int bound)
		{
			super();
			this.array = array;
			this.start = start;
			this.bound = bound;
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		protected final void internalSetStart(final int start)
		{
			this.start = XArrays.validateArrayIndex(this.array.length, start);
		}

		protected final void internalSetBound(final int bound)
		{
			this.bound = XArrays.validateArrayIndex(this.array.length, bound);
		}

		public Mutable setStart(final int start)
		{
			this.internalSetStart(start);
			return this;
		}

		public Mutable setBound(final int bound)
		{
			this.internalSetBound(bound);
			return this;
		}

		public Mutable set(final int start, final int bound)
		{
			this.internalSetStart(start);
			this.internalSetBound(bound);
			return this;
		}



		////////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final char[] array()
		{
			return this.array;
		}

		@Override
		public final int start()
		{
			return this.start;
		}

		@Override
		public final int bound()
		{
			return this.bound;
		}

	}

}
