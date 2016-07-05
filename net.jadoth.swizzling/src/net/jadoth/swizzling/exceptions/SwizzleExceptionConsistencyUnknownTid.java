package net.jadoth.swizzling.exceptions;


public class SwizzleExceptionConsistencyUnknownTid extends SwizzleExceptionConsistency
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	final long passedTid;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public SwizzleExceptionConsistencyUnknownTid(final long passedTid)
	{
		super();
		this.passedTid = passedTid;
	}



}
