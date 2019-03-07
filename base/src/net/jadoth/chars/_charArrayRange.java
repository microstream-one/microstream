package net.jadoth.chars;

import net.jadoth.collections.XArrays;



public interface _charArrayRange
{
	public char[] array();

	public int start();

	public int bound();


	public static Implementation New(final String value)
	{
		return New(value.toCharArray());
	}

	public static Implementation New(final char[] array)
	{
		return new Implementation(array, 0, array.length);
	}

	public static Implementation New(final char[] array, final int offset)
	{
		return new Implementation(
			array,
			XArrays.validateArrayIndex(array.length, offset),
			array.length
		);
	}

	public static Implementation New(final char[] array, final int offset, final int bound)
	{
		return new Implementation(
			array,
			XArrays.validateArrayIndex(array.length, offset),
			XArrays.validateArrayIndex(array.length, bound - 1) + 1 // nasty
		);
	}



	public final class Implementation implements _charArrayRange
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

		Implementation(final char[] array, final int start, final int bound)
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
		// static methods    //
		/////////////////////

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
