package one.microstream.storage.restadapter.types;

import one.microstream.persistence.types.Persistence;

public class ObjectReferenceWrapper
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private long objectId;


	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ObjectReferenceWrapper(final long objectId)
	{
		super();
		this.setObjectId(objectId);
	}


	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	public long getObjectId()
	{
		return this.objectId;
	}

	public void setObjectId(final long objectId)
	{
		this.objectId = objectId;
	}

	public boolean isValidObjectReference()
	{
		return Persistence.IdType.OID.isInRange(this.objectId);
	}

	public  boolean isValidConstantReference()
	{
		return Persistence.IdType.CID.isInRange(this.objectId);
	}
}
