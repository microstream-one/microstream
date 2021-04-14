package one.microstream.typing;

public interface _longKeyValue
{
	public long key();
	public long value();



	public class Default implements _longKeyValue
	{
		private final long key;
		private final long value;

		public Default(final long key, final long value)
		{
			super();
			this.key = key;
			this.value = value;
		}

		@Override
		public long key()
		{
			return this.key;
		}

		@Override
		public long value()
		{
			return this.value;
		}

		@Override
		public String toString()
		{
			return "(" + this.key + " -> " + this.value + ")";
		}

	}

}
