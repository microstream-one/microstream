package one.microstream.examples.layeredentities._Address;

import one.microstream.entity.EntityLayerIdentity;
import one.microstream.entity.Entity;
import one.microstream.examples.layeredentities.Address;
import java.lang.String;


public interface AddressCreator extends Entity.Creator<Address, AddressCreator>
{
	public AddressCreator street(String street);

	public AddressCreator city(String city);

	public AddressCreator zipCode(String zipCode);

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
		private String street ;
		private String city   ;
		private String zipCode;

		protected Default()
		{
			super();
		}

		@Override
		public AddressCreator street(final String street)
		{
			this.street = street;
			return this;
		}

		@Override
		public AddressCreator city(final String city)
		{
			this.city = city;
			return this;
		}

		@Override
		public AddressCreator zipCode(final String zipCode)
		{
			this.zipCode = zipCode;
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
				this.street ,
				this.city   ,
				this.zipCode);
		}

		@Override
		public AddressCreator copy(final Address other)
		{
			final Address data = Entity.data(other);
			this.street  = data.street ();
			this.city    = data.city   ();
			this.zipCode = data.zipCode();
			return this;
		}
	}
}