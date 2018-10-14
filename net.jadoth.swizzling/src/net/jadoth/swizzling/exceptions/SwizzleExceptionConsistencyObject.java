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



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public SwizzleExceptionConsistencyObject(
		final long   oid,
		final Object actualRef,
		final Object passedRef
	)
	{
		super();
		this.oid       = oid      ;
		this.actualRef = actualRef;
		this.passedRef = passedRef;
	}

	@Override
	public String getMessage()
	{
		return "oid = " + this.oid
			+ " actualRef = " + XChars.systemString(this.actualRef)
			+ " passedRef = " + XChars.systemString(this.passedRef)
		;
	}

}
