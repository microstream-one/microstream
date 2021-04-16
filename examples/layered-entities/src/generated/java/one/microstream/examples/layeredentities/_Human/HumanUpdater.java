package one.microstream.examples.layeredentities._Human;

import one.microstream.examples.layeredentities.Human;
import one.microstream.entity.Entity;
import one.microstream.examples.layeredentities.Address;
import java.lang.String;


public interface HumanUpdater extends Entity.Updater<Human, HumanUpdater>
{
	public static boolean setAddress(final Human human, final Address address)
	{
		return New(human).address(address).update();
	}

	public static boolean setPartner(final Human human, final Human partner)
	{
		return New(human).partner(partner).update();
	}

	public static boolean setName(final Human human, final String name)
	{
		return New(human).name(name).update();
	}

	public HumanUpdater address(Address address);

	public HumanUpdater partner(Human partner);

	public HumanUpdater name(String name);

	public static HumanUpdater New(final Human human)
	{
		return new Default(human);
	}

	public class Default
		extends Entity.Updater.Abstract<Human, HumanUpdater>
		implements HumanUpdater
	{
		private Address address;
		private Human   partner;
		private String  name   ;

		protected Default(final Human human)
		{
			super(human);
		}

		@Override
		public HumanUpdater address(final Address address)
		{
			this.address = address;
			return this;
		}

		@Override
		public HumanUpdater partner(final Human partner)
		{
			this.partner = partner;
			return this;
		}

		@Override
		public HumanUpdater name(final String name)
		{
			this.name = name;
			return this;
		}

		@Override
		public Human createData(final Human entityInstance)
		{
			return new HumanData(entityInstance,
				this.address,
				this.partner,
				this.name   );
		}

		@Override
		public HumanUpdater copy(final Human other)
		{
			final Human data = Entity.data(other);
			this.address = data.address();
			this.partner = data.partner();
			this.name    = data.name   ();
			return this;
		}
	}
}