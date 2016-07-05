package net.jadoth.swizzling.exceptions;



public class SwizzleExceptionConsistencyUnknownMapping extends SwizzleExceptionConsistency
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	final long     passedTid ;
	final Class<?> passedType;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public SwizzleExceptionConsistencyUnknownMapping(final long passedTid, final Class<?> passedType)
	{
		super();
		this.passedTid  = passedTid ;
		this.passedType = passedType;
	}



}
