package net.jadoth.swizzling.exceptions;

public class SwizzleExceptionConsistencyInvalidTypeId extends SwizzleExceptionConsistencyInvalidId
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public SwizzleExceptionConsistencyInvalidTypeId(final long id)
	{
		super(id);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public String getMessage()
	{
		return "Invalid type id: " + this.getId()
			+ (super.getMessage() != null ? " Details: " + super.getMessage() : "")
		;
	}



}
