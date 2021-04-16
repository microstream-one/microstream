package one.microstream.examples.layeredentities._Address;

import one.microstream.entity.Entity;
import one.microstream.examples.layeredentities.Address;
import java.lang.String;


public interface AddressUpdater extends Entity.Updater<Address, AddressUpdater>
{
	public static boolean setStreet(final Address address, final String street)
	{
		return New(address).street(street).update();
	}

	public static boolean setCity(final Address address, final String city)
	{
		return New(address).city(city).update();
	}

	public static boolean setZipCode(final Address address, final String zipCode)
	{
		return New(address).zipCode(zipCode).update();
	}

	public AddressUpdater street(String street);

	public AddressUpdater city(String city);

	public AddressUpdater zipCode(String zipCode);

	public static AddressUpdater New(final Address address)
	{
		return new Default(address);
	}

	public class Default
		extends Entity.Updater.Abstract<Address, AddressUpdater>
		implements AddressUpdater
	{
		private String street ;
		private String city   ;
		private String zipCode;

		protected Default(final Address address)
		{
			super(address);
		}

		@Override
		public AddressUpdater street(final String street)
		{
			this.street = street;
			return this;
		}

		@Override
		public AddressUpdater city(final String city)
		{
			this.city = city;
			return this;
		}

		@Override
		public AddressUpdater zipCode(final String zipCode)
		{
			this.zipCode = zipCode;
			return this;
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
		public AddressUpdater copy(final Address other)
		{
			final Address data = Entity.data(other);
			this.street  = data.street ();
			this.city    = data.city   ();
			this.zipCode = data.zipCode();
			return this;
		}
	}
}