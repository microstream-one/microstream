package net.jadoth.swizzling.exceptions;

public class SwizzleExceptionConsistencyTid extends SwizzleExceptionConsistency
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

	public SwizzleExceptionConsistencyTid(
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
	// declared methods //
	/////////////////////

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

	/**
	 * Sadly, the Throwable implementation uses #getMessage() directly to print the exception.
	 * This is a concern conflict: getMessage should actually be the getter for the explicit message.
	 * But it is used as the String representating method as well.
	 * So a output message generically assembling the output string must override the getter.
	 * As this hides the actual getting functionality, a workaround accessor method has to be provided
	 * for potential subclasses.
	 *
	 * @return the explicit message string passed to the constructor when creating this instance.
	 */
	// I love noobish JDK developers. And why is the adjective "Throwable" not an interface in the first place.
	public final String message()
	{
		return super.getMessage();
	}

	public String assembleDetailString()
	{
		return "OID=" + this.oid() + ", actualTID=" + this.actualTid() + ", passedTID=" + this.passedTid();
	}

	protected String assembleExplicitMessageAddon()
	{
		final String explicitMessage = super.getMessage();
		return explicitMessage == null ? "" : " (" + explicitMessage + ")";
	}

	public String assembleOutputString()
	{
		return this.assembleDetailString() + this.assembleExplicitMessageAddon();
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	/**
	 * Returns an assembled output String due to bad method design in {@link Throwable}.
	 * For the actual message getter, see {@link #message()}.
	 *
	 * @return this exception type's generic string plus an explicit message if present.
	 */
	@Override
	public String getMessage() // intentionally not final to enable subclasses to change the behaviour again
	{
		return this.assembleOutputString();
	}



}
