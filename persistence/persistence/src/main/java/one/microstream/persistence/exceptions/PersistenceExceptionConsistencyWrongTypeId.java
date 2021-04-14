package one.microstream.persistence.exceptions;



public class PersistenceExceptionConsistencyWrongTypeId extends PersistenceExceptionConsistency
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final Class<?> type     ;
	final long     actualTid;
	final long     passedTid;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionConsistencyWrongTypeId(final Class<?> type, final long actualTid, final long passedTid)
	{
		super();
		this.type      = type     ;
		this.actualTid = actualTid;
		this.passedTid = passedTid;
	}

	@Override
	public String getMessage()
	{
		return "Wrong type id for " + this.type + ": actual tid: " + this.actualTid + ", passed tid: " + this.passedTid
			+ (super.getMessage() != null ? " Details: " + super.getMessage() : "")
		;
	}



}
