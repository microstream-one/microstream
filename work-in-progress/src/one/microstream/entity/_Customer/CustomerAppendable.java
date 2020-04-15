package one.microstream.entity._Customer;

import one.microstream.chars.VarString;
import one.microstream.entity.Customer;


public interface CustomerAppendable extends VarString.Appendable
{
	public static String toString(final Customer customer)
	{
		return New(customer).appendTo(VarString.New()).toString();
	}

	public static CustomerAppendable New(final Customer customer)
	{
		return new Default(customer);
	}

	public static class Default implements CustomerAppendable
	{
		private final Customer customer;

		Default(final Customer customer)
		{
			super();

			this.customer = customer;
		}

		@Override
		public VarString appendTo(final VarString vs)
		{
			return vs.add(this.customer.getClass().getSimpleName())
				.add(" [firstName = ")
				.add(this.customer.firstName())
				.add(", lastName = ")
				.add(this.customer.lastName())
				.add(", address = ")
				.add(this.customer.address())
				.add(']');
		}
	}
}