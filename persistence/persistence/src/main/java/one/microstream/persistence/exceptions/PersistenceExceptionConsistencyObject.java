package one.microstream.persistence.exceptions;

import one.microstream.chars.XChars;

public class PersistenceExceptionConsistencyObject extends PersistenceExceptionConsistency
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final Object actualRef;
	final Object passedRef;
	final long   oid      ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionConsistencyObject(
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
