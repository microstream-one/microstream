package one.microstream.persistence.exceptions;

public class PersistenceExceptionConsistencyTid extends PersistenceExceptionConsistency
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	final Object reference;
	final long   oid      ;
	final long   actualTid;
	final long   passedTid;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public PersistenceExceptionConsistencyTid(
		final long   oid      ,
		final long   actualTid,
		final long   passedTid,
		final Object reference
	)
	{
		super();
		this.reference = reference;
		this.oid       = oid      ;
		this.actualTid = actualTid;
		this.passedTid = passedTid;
	}


	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	public Object reference()
	{
		return this.reference;
	}

	public long oid()
	{
		return this.oid;
	}

	public long actualTid()
	{
		return this.actualTid;
	}

	public long passedTid()
	{
		return this.passedTid;
	}

	@Override
	public String assembleDetailString()
	{
		return "OID = " + this.oid() + ", actualTID = " + this.actualTid() + ", passedTID = " + this.passedTid();
	}

}
