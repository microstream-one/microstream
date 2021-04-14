package one.microstream.persistence.exceptions;

public class PersistenceExceptionConsistencyTid extends PersistenceExceptionConsistency
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final Object reference   ;
	final long   objectId    ;
	final long   actualTypeId;
	final long   passedTypeId;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionConsistencyTid(
		final long   objectId ,
		final long   actualTypeId,
		final long   passedTypeId,
		final Object reference
	)
	{
		super();
		this.reference    = reference   ;
		this.objectId     = objectId    ;
		this.actualTypeId = actualTypeId;
		this.passedTypeId = passedTypeId;
	}

	

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	public Object reference()
	{
		return this.reference;
	}

	public long objectId()
	{
		return this.objectId;
	}

	public long actualTypeId()
	{
		return this.actualTypeId;
	}

	public long passedTypeId()
	{
		return this.passedTypeId;
	}

	@Override
	public String assembleDetailString()
	{
		return "ObjectId = " + this.objectId()
			+ ", actual TypeId = " + this.actualTypeId()
			+ ", passed TypeId = " + this.passedTypeId()
		;
	}

}
