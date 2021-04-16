package one.microstream.examples.layeredentities._Address;

import one.microstream.examples.layeredentities.Address;
import one.microstream.chars.VarString;


public interface AddressAppendable extends VarString.Appendable
{
	public static String toString(final Address address)
	{
		return New(address).appendTo(VarString.New()).toString();
	}

	public static AddressAppendable New(final Address address)
	{
		return new Default(address);
	}

	public static class Default implements AddressAppendable
	{
		private final Address address;

		Default(final Address address)
		{
			super();

			this.address = address;
		}

		@Override
		public VarString appendTo(final VarString vs)
		{
			return vs.add(this.address.getClass().getSimpleName())
				.add(" [street = ")
				.add(this.address.street())
				.add(", city = ")
				.add(this.address.city())
				.add(", zipCode = ")
				.add(this.address.zipCode())
				.add(']');
		}
	}
}