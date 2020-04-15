package one.microstream.entity._Address;

import one.microstream.entity.Address;
import one.microstream.entity.Entity;
import one.microstream.entity.EntityLayerIdentity;


public interface AddressCreator extends Entity.Creator<Address, AddressCreator>
{
	public AddressCreator line1(String line1);

	public AddressCreator line2(String line2);

	public AddressCreator city(String city);

	public static AddressCreator New()
	{
		return new Default();
	}

	public static AddressCreator New(final Address other)
	{
		return new Default().copy(other);
	}

	public class Default
		extends Entity.Creator.Abstract<Address, AddressCreator>
		implements AddressCreator
	{
		private String line1;
		private String line2;
		private String city ;

		protected Default()
		{
			super();
		}

		@Override
		public AddressCreator line1(final String line1)
		{
			this.line1 = line1;
			return this;
		}

		@Override
		public AddressCreator line2(final String line2)
		{
			this.line2 = line2;
			return this;
		}

		@Override
		public AddressCreator city(final String city)
		{
			this.city = city;
			return this;
		}

		@Override
		protected EntityLayerIdentity createEntityInstance()
		{
			return new AddressEntity();
		}

		@Override
		public Address createData(final Address entityInstance)
		{
			return new AddressData(entityInstance,
				this.line1,
				this.line2,
				this.city );
		}

		@Override
		public AddressCreator copy(final Address other)
		{
			final Address data = Entity.data(other);
			this.line1 = data.line1();
			this.line2 = data.line2();
			this.city  = data.city ();
			return this;
		}
	}
}