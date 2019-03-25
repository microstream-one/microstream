package one.microstream.persistence.exceptions;

public class PersistenceExceptionConsistencyInvalidTypeId extends PersistenceExceptionConsistencyInvalidId
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionConsistencyInvalidTypeId(final long id)
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
