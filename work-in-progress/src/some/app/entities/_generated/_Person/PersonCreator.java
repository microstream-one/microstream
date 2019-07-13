package some.app.entities._generated._Person;

import one.microstream.entity.Entity;
import one.microstream.entity.EntityLayerIdentity;
import some.app.entities.Person;

public interface PersonCreator extends Entity.Creator<Person,PersonCreator>
{
	public PersonCreator firstName(String value);
	
	public PersonCreator lastName(String value);
	
	
	public static PersonCreator New()
	{
		return new PersonCreator.Default();
	}
	
	public static PersonCreator New(final Person other)
	{
		return New().entity(other).copy(other);
	}
	
	
	
	public class Default
	extends Entity.Creator.Abstract<Person,PersonCreator>
	implements PersonCreator
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private String firstName;
		private String lastName ;

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public PersonCreator firstName(final String value)
		{
			this.firstName = value;
			return this;
		}

		@Override
		public PersonCreator lastName(final String value)
		{
			this.lastName = value;
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
			return new PersonData(entityInstance, this.firstName, this.lastName);
		}
		
		@Override
		public PersonCreator copy(final Person other)
		{
			final Person data = (Person)other.$data();
			this.firstName = data.firstName();
			this.lastName  = data.lastName() ;
			
			return this;
		}
		
	}
	
}
