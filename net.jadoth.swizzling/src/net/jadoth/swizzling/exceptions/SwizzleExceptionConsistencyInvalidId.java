package net.jadoth.swizzling.exceptions;

public class SwizzleExceptionConsistencyInvalidId extends SwizzleExceptionConsistency
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	final long id;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public SwizzleExceptionConsistencyInvalidId(final long id)
	{
		super();
		this.id = id;
	}
	
	public SwizzleExceptionConsistencyInvalidId(final long id, final String message)
	{
		super(message);
		this.id = id;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters          //
	/////////////////////

	public final long getId()
	{
		return this.id;
	}

}
