package one.microstream.examples.layeredentities._Human;

import one.microstream.entity.EntityLayerIdentity;
import one.microstream.examples.layeredentities.Human;
import one.microstream.entity.Entity;
import one.microstream.examples.layeredentities.Address;
import java.lang.String;


public interface HumanCreator extends Entity.Creator<Human, HumanCreator>
{
	public HumanCreator address(Address address);

	public HumanCreator partner(Human partner);

	public HumanCreator name(String name);

	public static HumanCreator New()
	{
		return new Default();
	}

	public static HumanCreator New(final Human other)
	{
		return new Default().copy(other);
	}

	public class Default
		extends Entity.Creator.Abstract<Human, HumanCreator>
		implements HumanCreator
	{
		private Address address;
		private Human   partner;
		private String  name   ;

		protected Default()
		{
			super();
		}

		@Override
		public HumanCreator address(final Address address)
		{
			this.address = address;
			return this;
		}

		@Override
		public HumanCreator partner(final Human partner)
		{
			this.partner = partner;
			return this;
		}

		@Override
		public HumanCreator name(final String name)
		{
			this.name = name;
			return this;
		}

		@Override
		protected EntityLayerIdentity createEntityInstance()
		{
			return new HumanEntity();
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
		public HumanCreator copy(final Human other)
		{
			final Human data = Entity.data(other);
			this.address = data.address();
			this.partner = data.partner();
			this.name    = data.name   ();
			return this;
		}
	}
}