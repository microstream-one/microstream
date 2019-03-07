package one.microstream.reference;

public interface _longReference
{
	public long get();



	public static Constant Constant(final long value)
	{
		return new Constant(value);
	}

	public final class Constant implements _longReference
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final long value;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Constant(final long value)
		{
			super();
			this.value = value;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final long get()
		{
			return this.value;
		}

	}

}
