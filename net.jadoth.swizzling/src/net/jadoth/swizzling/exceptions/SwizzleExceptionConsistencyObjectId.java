package net.jadoth.swizzling.exceptions;

public class SwizzleExceptionConsistencyObjectId extends SwizzleExceptionConsistency
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	final Object reference;
	final long   actualOid;
	final long   passedOid;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public SwizzleExceptionConsistencyObjectId(final Object reference, final long actualOid, final long passedOid)
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
