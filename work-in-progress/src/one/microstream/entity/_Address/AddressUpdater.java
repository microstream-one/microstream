package one.microstream.entity._Address;

import one.microstream.entity.Address;
import one.microstream.entity.Entity;


public interface AddressUpdater extends Entity.Updater<Address, AddressUpdater>
{
	public static boolean setLine1(final Address address, final String line1)
	{
		return New(address).line1(line1).update();
	}

	public static boolean setLine2(final Address address, final String line2)
	{
		return New(address).line2(line2).update();
	}

	public static boolean setCity(final Address address, final String city)
	{
		return New(address).city(city).update();
	}

	public AddressUpdater line1(String line1);

	public AddressUpdater line2(String line2);

	public AddressUpdater city(String city);

	public static AddressUpdater New(final Address address)
	{
		return new Default(address);
	}

	public class Default
		extends Entity.Updater.Abstract<Address, AddressUpdater>
		implements AddressUpdater
	{
		private String line1;
		private String line2;
		private String city ;

		protected Default(final Address address)
		{
			super(address);
		}

		@Override
		public AddressUpdater line1(final String line1)
		{
			this.line1 = line1;
			return this;
		}

		@Override
		public AddressUpdater line2(final String line2)
		{
			this.line2 = line2;
			return this;
		}

		@Override
		public AddressUpdater city(final String city)
		{
			this.city = city;
			return this;
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
		public AddressUpdater copy(final Address other)
		{
			final Address data = Entity.data(other);
			this.line1 = data.line1();
			this.line2 = data.line2();
			this.city  = data.city ();
			return this;
		}
	}
}