package one.microstream.persistence.exceptions;

public class PersistenceExceptionConsistencyObjectId extends PersistenceExceptionConsistency
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final Object reference;
	final long   actualOid;
	final long   passedOid;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionConsistencyObjectId(final Object reference, final long actualOid, final long passedOid)
	{
		super();
		this.reference = reference;
		this.actualOid = actualOid;
		this.passedOid = passedOid;
	}

	@Override
	public String getMessage()
	{
		return "Inconsistent Object id. Registered: " + this.actualOid + ", passed: " + this.passedOid;
	}



}
