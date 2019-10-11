package some.app.entities._generated._Person;

import one.microstream.entity.Entity;
import one.microstream.entity.EntityLayerIdentity;
import some.app.entities.Person;


public interface PersonCreator extends Entity.Creator<Person, PersonCreator>
{
	public PersonCreator firstName(String firstName);

	public PersonCreator lastName(String lastName);

	public static PersonCreator New()
	{
		return new Default();
	}

	public static PersonCreator New(final Person other)
	{
		return new Default().copy(other);
	}

	public class Default
		extends Entity.Creator.Abstract<Person, PersonCreator>
		implements PersonCreator
	{
		private String firstName;
		private String lastName ;

		protected Default()
		{
			super();
		}

		@Override
		public PersonCreator firstName(final String firstName)
		{
			this.firstName = firstName;
			return this;
		}

		@Override
		public PersonCreator lastName(final String lastName)
		{
			this.lastName = lastName;
			return this;
		}

		@Override
		protected EntityLayerIdentity createEntityInstance()
		{
			return new PersonEntity();
			}

		@Override
		public Person createData(final Person entityInstance)
		{
			return new PersonData(entityInstance,
				this.firstName,
				this.lastName );
		}

		@Override
		public PersonCreator copy(final Person other)
		{
			final Person data = Entity.data(other);
			this.firstName = data.firstName();
			this.lastName  = data.lastName ();
			return this;
		}
	}
}