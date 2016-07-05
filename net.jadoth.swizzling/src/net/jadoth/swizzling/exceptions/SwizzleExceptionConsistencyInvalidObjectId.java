package net.jadoth.swizzling.exceptions;

public class SwizzleExceptionConsistencyInvalidObjectId extends SwizzleExceptionConsistencyInvalidId
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public SwizzleExceptionConsistencyInvalidObjectId(final long id)
	{
		super(id);
	}

	public SwizzleExceptionConsistencyInvalidObjectId(final long id, final String message)
	{
		super(id, message);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public String getMessage()
	{
		return "Invalid object id: " + this.getId()
			+ (super.getMessage() != null ? " Details: " + super.getMessage() : "")
		;
	}

}
