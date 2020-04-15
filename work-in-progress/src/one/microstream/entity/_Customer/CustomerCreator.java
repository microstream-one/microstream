package one.microstream.entity._Customer;

import one.microstream.entity.Address;
import one.microstream.entity.Customer;
import one.microstream.entity.Entity;
import one.microstream.entity.EntityLayerIdentity;


public interface CustomerCreator extends Entity.Creator<Customer, CustomerCreator>
{
	public CustomerCreator firstName(String firstName);

	public CustomerCreator lastName(String lastName);

	public CustomerCreator address(Address address);

	public static CustomerCreator New()
	{
		return new Default();
	}

	public static CustomerCreator New(final Customer other)
	{
		return new Default().copy(other);
	}

	public class Default
		extends Entity.Creator.Abstract<Customer, CustomerCreator>
		implements CustomerCreator
	{
		private String  firstName;
		private String  lastName ;
		private Address address  ;

		protected Default()
		{
			super();
		}

		@Override
		public CustomerCreator firstName(final String firstName)
		{
			this.firstName = firstName;
			return this;
		}

		@Override
		public CustomerCreator lastName(final String lastName)
		{
			this.lastName = lastName;
			return this;
		}

		@Override
		public CustomerCreator address(final Address address)
		{
			this.address = address;
			return this;
		}

		@Override
		protected EntityLayerIdentity createEntityInstance()
		{
			return new CustomerEntity();
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
		public CustomerCreator copy(final Customer other)
		{
			final Customer data = Entity.data(other);
			this.firstName = data.firstName();
			this.lastName  = data.lastName ();
			this.address   = data.address  ();
			return this;
		}
	}
}