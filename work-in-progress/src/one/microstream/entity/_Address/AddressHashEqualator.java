package one.microstream.entity._Address;

import java.util.Objects;

import one.microstream.X;
import one.microstream.entity.Address;
import one.microstream.hashing.HashEqualator;
import one.microstream.typing.Stateless;


public interface AddressHashEqualator extends HashEqualator<Address>
{
	public static AddressHashEqualator New()
	{
		return new Default();
	}

	public static class Default implements AddressHashEqualator, Stateless
	{
		public static boolean equals(final Address address1, final Address address2)
		{
			return X.equal(address1.line1(), address2.line1())
				&& X.equal(address1.line2(), address2.line2())
				&& X.equal(address1.city (), address2.city ())
			;
		}

		public static int hashCode(final Address address)
		{
			return Objects.hash(
				address.line1(),
				address.line2(),
				address.city ()
			);
		}

		Default()
		{
			super();
		}

		@Override
		public boolean equal(final Address address1, final Address address2)
		{
			return equals(address1, address2);
		}

		@Override
		public int hash(final Address address)
		{
			return hashCode(address);
		}
	}
}