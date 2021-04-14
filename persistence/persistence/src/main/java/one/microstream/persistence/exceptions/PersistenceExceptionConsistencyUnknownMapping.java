package one.microstream.persistence.exceptions;



public class PersistenceExceptionConsistencyUnknownMapping extends PersistenceExceptionConsistency
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final long     passedTid ;
	final Class<?> passedType;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionConsistencyUnknownMapping(final long passedTid, final Class<?> passedType)
	{
		super();
		this.passedTid  = passedTid ;
		this.passedType = passedType;
	}



}
