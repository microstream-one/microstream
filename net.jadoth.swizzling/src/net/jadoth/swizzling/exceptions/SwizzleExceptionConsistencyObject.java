package net.jadoth.swizzling.exceptions;

import net.jadoth.chars.XChars;

public class SwizzleExceptionConsistencyObject extends SwizzleExceptionConsistency
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	final Object actualRef;
	final Object passedRef;
	final long   oid      ;
	final long   actualTid;
	final long   passedTid;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public SwizzleExceptionConsistencyObject(
		final long   oid,
		final long   actualTid,
		final long   passedTid,
		final Object actualRef,
		final Object passedRef
	)
	{
		super();
		this.oid       = oid      ;
		this.actualTid = actualTid;
		this.passedTid = passedTid;
		this.actualRef = actualRef;
		this.passedRef = passedRef;
	}

	@Override
	public String getMessage()
	{
		return "oid = " + this.oid
			+ " actualTid = " + this.actualTid
			+ " passedTid = " + this.passedTid
			+ " actualRef = " + XChars.systemString(this.actualRef)
			+ " passedRef = " + XChars.systemString(this.passedRef)
		;
	}



}
