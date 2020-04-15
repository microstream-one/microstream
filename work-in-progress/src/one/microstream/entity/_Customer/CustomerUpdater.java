package one.microstream.entity._Customer;

import one.microstream.entity.Address;
import one.microstream.entity.Customer;
import one.microstream.entity.Entity;


public interface CustomerUpdater extends Entity.Updater<Customer, CustomerUpdater>
{
	public static boolean setFirstName(final Customer customer, final String firstName)
	{
		return New(customer).firstName(firstName).update();
	}

	public static boolean setLastName(final Customer customer, final String lastName)
	{
		return New(customer).lastName(lastName).update();
	}

	public static boolean setAddress(final Customer customer, final Address address)
	{
		return New(customer).address(address).update();
	}

	public CustomerUpdater firstName(String firstName);

	public CustomerUpdater lastName(String lastName);

	public CustomerUpdater address(Address address);

	public static CustomerUpdater New(final Customer customer)
	{
		return new Default(customer);
	}

	public class Default
		extends Entity.Updater.Abstract<Customer, CustomerUpdater>
		implements CustomerUpdater
	{
		private String  firstName;
		private String  lastName ;
		private Address address  ;

		protected Default(final Customer customer)
		{
			super(customer);
		}

		@Override
		public CustomerUpdater firstName(final String firstName)
		{
			this.firstName = firstName;
			return this;
		}

		@Override
		public CustomerUpdater lastName(final String lastName)
		{
			this.lastName = lastName;
			return this;
		}

		@Override
		public CustomerUpdater address(final Address address)
		{
			this.address = address;
			return this;
		}

		@Override
		public Customer createData(final Customer entityInstance)
		{
			return new CustomerData(entityInstance,
				this.firstName,
				this.lastName ,
				this.address  );
		}

		@Override
		public CustomerUpdater copy(final Customer other)
		{
			final Customer data = Entity.data(other);
			this.firstName = data.firstName();
			this.lastName  = data.lastName ();
			this.address   = data.address  ();
			return this;
		}
	}
}