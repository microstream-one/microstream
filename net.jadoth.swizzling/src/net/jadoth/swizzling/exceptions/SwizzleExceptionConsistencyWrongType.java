package net.jadoth.swizzling.exceptions;



public class SwizzleExceptionConsistencyWrongType extends SwizzleExceptionConsistency
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	final long     tid       ;
	final Class<?> actualType;
	final Class<?> passedType;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public SwizzleExceptionConsistencyWrongType(final long tid, final Class<?> actualType, final Class<?> passedType)
	{
		super();
		this.tid        = tid       ;
		this.actualType = actualType;
		this.passedType = passedType;
	}



}
