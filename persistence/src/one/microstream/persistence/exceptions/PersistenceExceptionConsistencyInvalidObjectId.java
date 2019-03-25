package one.microstream.persistence.exceptions;

public class PersistenceExceptionConsistencyInvalidObjectId extends PersistenceExceptionConsistencyInvalidId
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionConsistencyInvalidObjectId(final long id)
	{
		super(id);
	}

	public PersistenceExceptionConsistencyInvalidObjectId(final long id, final String message)
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
