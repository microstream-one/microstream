package one.microstream.test.corp.model;

import one.microstream.X;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryLegacyTypeHandler;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceObjectIdResolver;

public class LegacyTypeHandlerPerson extends BinaryLegacyTypeHandler.AbstractCustom<Person>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////
	
	// ugly two-part prototype binary field definitions. See type "BinaryField" for overhauled version
	private static final long
		BINARY_OFFSET_contactId =                                                     0,
		BINARY_OFFSET_address   = BINARY_OFFSET_contactId + Binary.objectIdByteLength(),
		BINARY_OFFSET_note      = BINARY_OFFSET_address   + Binary.objectIdByteLength(),
		BINARY_OFFSET_firstname = BINARY_OFFSET_note      + Binary.objectIdByteLength(),
		BINARY_OFFSET_lastname  = BINARY_OFFSET_firstname + Binary.objectIdByteLength()
	;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public LegacyTypeHandlerPerson()
	{
		super(Person.class,
			X.List(
				// ugly two-part prototype binary field definitions. See type "BinaryField" for overhauled version
				CustomField(String.class , "contactId"),
				CustomField(Address.class, "address"  ),
				CustomField(String.class , "note"     ),
				CustomField(String.class , "firstname"),
				CustomField(String.class , "lastname" )
			)
		);
		
		// optionally initializeable to a specific TypeId ("type version"). Otherwise, TypeId is assigned implicitely.
//		this.initialize(1000040);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public Person create(final Binary bytes, final PersistenceObjectIdResolver idResolver)
	{
		// required instances may not be available, yet, at creation time. Thus create dummy and fill in #update.
		return new Person();
	}
	
	@Override
	public void update(final Binary bytes, final Person instance, final PersistenceObjectIdResolver idResolver)
	{
		/*
		 * data updating logic for unchanged type. Custom legacy mapping would be done here. E.g. transform
		 * String note to an instance of type "Note":
		 * instance.setNote(new Note(note));
		 */
		
		final String  contactId =  (String)idResolver.lookupObject(bytes.get_long(BINARY_OFFSET_contactId));
		final Address address   = (Address)idResolver.lookupObject(bytes.get_long(BINARY_OFFSET_address  ));
		final String  note      =  (String)idResolver.lookupObject(bytes.get_long(BINARY_OFFSET_note     ));
		final String  firstname =  (String)idResolver.lookupObject(bytes.get_long(BINARY_OFFSET_firstname));
		final String  lastname  =  (String)idResolver.lookupObject(bytes.get_long(BINARY_OFFSET_lastname ));

		instance.internalSetContactId(contactId);
		instance.setAddress          (address)  ;
		instance.setFirstname        (firstname);
		instance.setLastname         (lastname) ;
		instance.setNote             (note)     ;
	}

	@Override
	public final void iterateLoadableReferences(
		final Binary                      bytes   ,
		final PersistenceObjectIdAcceptor iterator
	)
	{
		// clumsy offset code redundancy to be replaced by BinaryField w.i.p concept ...
		iterator.acceptObjectId(bytes.get_long(BINARY_OFFSET_contactId));
		iterator.acceptObjectId(bytes.get_long(BINARY_OFFSET_address  ));
		iterator.acceptObjectId(bytes.get_long(BINARY_OFFSET_note     ));
		iterator.acceptObjectId(bytes.get_long(BINARY_OFFSET_firstname));
		iterator.acceptObjectId(bytes.get_long(BINARY_OFFSET_lastname ));
	}

	@Override
	public boolean hasInstanceReferences()
	{
		// runtime instances have references to other entities
		return true;
	}

	@Override
	public boolean hasPersistedReferences()
	{
		// persisted data records have references to other persisted data records
		return true;
	}

	@Override
	public boolean hasVaryingPersistedLengthInstances()
	{
		/* the same instance can never have a varying persisted length.
		 * E.g. persisted data records of Person are always exactely 64 bytes long:
		 * 24 bytes header (length, typeId, objectId) + 5*8 for the five references.
		 * 
		 * Collections are an example for variable length instances.
		 * The same collection instance can contain 2 elements at one store and 3 at another store.
		 */
		return false;
	}
	
}
