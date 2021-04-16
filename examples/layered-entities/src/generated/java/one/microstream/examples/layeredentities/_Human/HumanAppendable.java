package one.microstream.examples.layeredentities._Human;

import one.microstream.examples.layeredentities.Human;
import one.microstream.chars.VarString;


public interface HumanAppendable extends VarString.Appendable
{
	public static String toString(final Human human)
	{
		return New(human).appendTo(VarString.New()).toString();
	}

	public static HumanAppendable New(final Human human)
	{
		return new Default(human);
	}

	public static class Default implements HumanAppendable
	{
		private final Human human;

		Default(final Human human)
		{
			super();

			this.human = human;
		}

		@Override
		public VarString appendTo(final VarString vs)
		{
			return vs.add(this.human.getClass().getSimpleName())
				.add(" [address = ")
				.add(this.human.address())
				.add(", partner = ")
				.add(this.human.partner())
				.add(", name = ")
				.add(this.human.name())
				.add(']');
		}
	}
}