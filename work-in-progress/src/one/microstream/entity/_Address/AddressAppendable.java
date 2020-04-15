package one.microstream.entity._Address;

import one.microstream.chars.VarString;
import one.microstream.entity.Address;


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
				.add(" [line1 = ")
				.add(this.address.line1())
				.add(", line2 = ")
				.add(this.address.line2())
				.add(", city = ")
				.add(this.address.city())
				.add(']');
		}
	}
}