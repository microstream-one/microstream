package some.app.entities._generated._Person;

import one.microstream.entity.Entity;
import some.app.entities.Person;


public interface PersonUpdater extends Entity.Updater<Person, PersonUpdater>
{
	public static boolean setFirstName(final Person person, final String firstName)
	{
		return New(person).firstName(firstName).update();
	}

	public static boolean setLastName(final Person person, final String lastName)
	{
		return New(person).lastName(lastName).update();
	}

	public PersonUpdater firstName(String firstName);

	public PersonUpdater lastName(String lastName);

	public static PersonUpdater New(final Person person)
	{
		return new Default(person);
	}

	public class Default
		extends Entity.Updater.Abstract<Person, PersonUpdater>
		implements PersonUpdater
	{
		private String firstName;
		private String lastName ;

		protected Default(final Person person)
		{
			super(person);
		}

		@Override
		public PersonUpdater firstName(final String firstName)
		{
			this.firstName = firstName;
			return this;
		}

		@Override
		public PersonUpdater lastName(final String lastName)
		{
			this.lastName = lastName;
			return this;
		}

		@Override
		public Person createData(final Person entityInstance)
		{
			return new PersonData(entityInstance,
				this.firstName,
				this.lastName );
		}

		@Override
		public PersonUpdater copy(final Person other)
		{
			final Person data = Entity.data(other);
			this.firstName = data.firstName();
			this.lastName  = data.lastName ();
			return this;
		}
	}
}