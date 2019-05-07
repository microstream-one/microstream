package one.microstream.persistence.exceptions;


public class PersistenceExceptionConsistencyUnknownTid extends PersistenceExceptionConsistency
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final long passedTid;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionConsistencyUnknownTid(final long passedTid)
	{
		super();
		this.passedTid = passedTid;
	}



}
