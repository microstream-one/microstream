package one.microstream.test.corp.model;

import one.microstream.collections.EqHashTable;
import one.microstream.collections.HashEnum;
import one.microstream.collections.types.XSequence;
import one.microstream.collections.types.XTable;


public interface Customer extends Contact
{
	public XTable<String, Order> orders();

	public XSequence<Address> shippingAddresses();

	public Address billingAddress();

	public void setBillingAddress(Address billingAddress);



	public abstract class Abstract implements Customer
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

		public Abstract()
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
