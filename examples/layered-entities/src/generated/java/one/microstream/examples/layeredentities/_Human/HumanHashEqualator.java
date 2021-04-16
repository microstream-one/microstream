package one.microstream.examples.layeredentities._Human;

import one.microstream.examples.layeredentities.Human;
import one.microstream.typing.Stateless;
import java.util.Objects;
import one.microstream.X;
import one.microstream.hashing.HashEqualator;


public interface HumanHashEqualator extends HashEqualator<Human>
{
	public static HumanHashEqualator New()
	{
		return new Default();
	}

	public static class Default implements HumanHashEqualator, Stateless
	{
		public static boolean equals(final Human human1, final Human human2)
		{
			return X.equal(human1.address(), human2.address())
				&& X.equal(human1.partner(), human2.partner())
				&& X.equal(human1.name   (), human2.name   ())
			;
		}

		public static int hashCode(final Human human)
		{
			return Objects.hash(
				human.address(),
				human.partner(),
				human.name   ()
			);
		}

		Default()
		{
			super();
		}

		@Override
		public boolean equal(final Human human1, final Human human2)
		{
			return equals(human1, human2);
		}

		@Override
		public int hash(final Human human)
		{
			return hashCode(human);
		}
	}
}