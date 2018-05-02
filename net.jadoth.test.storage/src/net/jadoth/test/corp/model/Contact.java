package net.jadoth.test.corp.model;

import net.jadoth.storage.util.StoreEager;

public interface Contact
{
	public String contactId();

	public String name();

	public Person person();

	public Address address();

	public String note();

	public void setAddress(Address address);

	public void setNote(String note);



	public abstract class AbstractImplementation implements Contact
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final String  contactId;
		
		@StoreEager
		private       Address address  ;
		private       String  note     ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public AbstractImplementation(final String contactId, final Address address)
		{
			super();
			this.contactId = contactId;
			this.address = address;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public String contactId()
		{
			return this.contactId;
		}

		@Override
		public Address address()
		{
			return this.address;
		}

		@Override
		public String note()
		{
			return this.note;
		}

		@Override
		public void setAddress(final Address address)
		{
			this.address = address;
		}

		@Override
		public void setNote(final String note)
		{
			this.note = note;
		}

		@Override
		public String toString()
		{
			return this.name();
		}

	}

}
