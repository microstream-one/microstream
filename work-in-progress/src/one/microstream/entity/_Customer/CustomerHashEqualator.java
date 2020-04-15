
package one.microstream.entity._Customer;

import java.util.Objects;

import one.microstream.X;
import one.microstream.entity.Customer;
import one.microstream.hashing.HashEqualator;
import one.microstream.typing.Stateless;


public interface CustomerHashEqualator extends HashEqualator<Customer>
{
	public static CustomerHashEqualator New()
	{
		return new Default();
	}

	public static class Default implements CustomerHashEqualator, Stateless
	{
		public static boolean equals(final Customer customer1, final Customer customer2)
		{
			return X.equal(customer1.firstName(), customer2.firstName())
				&& X.equal(customer1.lastName (), customer2.lastName ())
				&& X.equal(customer1.address  (), customer2.address  ())
			;
		}

		public static int hashCode(final Customer customer)
		{
			return Objects.hash(
				customer.firstName(),
				customer.lastName (),
				customer.address  ()
			);
		}

		Default()
		{
			super();
		}

		@Override
		public boolean equal(final Customer customer1, final Customer customer2)
		{
			return equals(customer1, customer2);
		}

		@Override
		public int hash(final Customer customer)
		{
			return hashCode(customer);
		}
	}
}