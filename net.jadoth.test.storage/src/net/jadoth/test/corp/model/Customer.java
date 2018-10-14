package net.jadoth.test.corp.model;

import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.HashEnum;
import net.jadoth.collections.types.XSequence;
import net.jadoth.collections.types.XTable;


public interface Customer extends Contact
{
	public XTable<String, Order> orders();

	public XSequence<Address> shippingAddresses();

	public Address billingAddress();

	public void setBillingAddress(Address billingAddress);



	public abstract class AbstractImplementation implements Customer
	{
		////////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final XTable<String, Order> orders           ;
		final XSequence<Address>    shippingAddresses;
		      Address               billingAddress   ;



		////////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public AbstractImplementation()
		{
			super();
			this.orders            = EqHashTable.New();
			this.shippingAddresses = HashEnum.New()   ;
			this.billingAddress    = null             ;
		}



		////////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final XTable<String, Order> orders()
		{
			return this.orders;
		}

		@Override
		public final XSequence<Address> shippingAddresses()
		{
			return this.shippingAddresses;
		}

		@Override
		public final Address billingAddress()
		{
			return this.billingAddress;
		}

		@Override
		public void setBillingAddress(final Address billingAddress)
		{
			this.billingAddress = billingAddress;
		}

	}

}
