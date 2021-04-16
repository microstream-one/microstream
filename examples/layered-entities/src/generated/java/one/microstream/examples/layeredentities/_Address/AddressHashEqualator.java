package one.microstream.examples.layeredentities._Address;

import one.microstream.typing.Stateless;
import one.microstream.examples.layeredentities.Address;
import java.util.Objects;
import one.microstream.X;
import one.microstream.hashing.HashEqualator;


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
			return X.equal(address1.street (), address2.street ())
				&& X.equal(address1.city   (), address2.city   ())
				&& X.equal(address1.zipCode(), address2.zipCode())
			;
		}

		public static int hashCode(final Address address)
		{
			return Objects.hash(
				address.street (),
				address.city   (),
				address.zipCode()
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