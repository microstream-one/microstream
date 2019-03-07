package some.app.entities._generated._Person;

import one.microstream.entity.Entity;
import one.microstream.entity.EntityLayerIdentity;
import some.app.entities.Person;

public interface Creator_Person extends Entity.Creator<Person, Creator_Person>
{
	public Creator_Person firstName(String value);
	
	public Creator_Person lastName(String value);
	
	public Creator_Person copy(Person other);
	
	@Override
	public Creator_Person entity(Entity<Person> identity);
	
	@Override
	public Person create();
	
	@Override
	public Person createData(Person entityInstance);
	
	
	public static Creator_Person New()
	{
		return new Creator_Person.Implementation();
	}
	
	public static Creator_Person New(final Person other)
	{
		return New().entity(other).copy(other);
	}
	
	
	
	public class Implementation
	extends Entity.Creator.Implementation<Person, Creator_Person>
	implements Creator_Person
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
		protected EntityLayerIdentity<Person> createEntityInstance()
		{
			return new _Entity();
		}
		
		@Override
		public Person createData(final Person entityInstance)
		{
			return new _Data(entityInstance, this.firstName, this.lastName);
		}
		
		@Override
		public Creator_Person firstName(final String value)
		{
			this.firstName = value;
			return this;
		}

		@Override
		public Creator_Person lastName(final String value)
		{
			this.lastName = value;
			return this;
		}
		
		@Override
		public Creator_Person entity(final Entity<Person> entity)
		{
			super.entity(entity);
			return this;
		}
		
		@Override
		public Creator_Person copy(final Person other)
		{
			final Person data = other.$data();
			this.firstName = data.firstName();
			this.lastName  = data.lastName() ;
			
			return this;
		}
		
	}
	
}
