package one.microstream.reference;

@FunctionalInterface
public interface _longReference
{
	public long get();



	public static _longReference New(final long value)
	{
		return new Default(value);
	}

	public final class Default implements _longReference
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final long value;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final long value)
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
