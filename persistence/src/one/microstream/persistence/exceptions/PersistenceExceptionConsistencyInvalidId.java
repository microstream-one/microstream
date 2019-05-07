package one.microstream.persistence.exceptions;

public class PersistenceExceptionConsistencyInvalidId extends PersistenceExceptionConsistency
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final long id;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionConsistencyInvalidId(final long id)
	{
		super();
		this.id = id;
	}
	
	public PersistenceExceptionConsistencyInvalidId(final long id, final String message)
	{
		super(message);
		this.id = id;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public final long getId()
	{
		return this.id;
	}

}
