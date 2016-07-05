
package net.jadoth.persistence.exceptions;


public class PersistenceExceptionTypeConsistencyDefinitionResolveTypeName
extends PersistenceExceptionTypeConsistencyDictionary
{
	///////////////////////////////////////////////////////////////////////////
	// instance typeNames  //
	/////////////////////

	private final String typeName;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public PersistenceExceptionTypeConsistencyDefinitionResolveTypeName(final String typeName)
	{
		this(typeName, null, null);
	}

	public PersistenceExceptionTypeConsistencyDefinitionResolveTypeName(final String typeName, final String message)
	{
		this(typeName, message, null);
	}

	public PersistenceExceptionTypeConsistencyDefinitionResolveTypeName(final String typeName, final Throwable cause)
	{
		this(typeName, null, cause);
	}

	public PersistenceExceptionTypeConsistencyDefinitionResolveTypeName(
		final String    typeName,
		final String    message ,
		final Throwable cause
	)
	{
		this(typeName, message, cause, true, true);
	}

	public PersistenceExceptionTypeConsistencyDefinitionResolveTypeName(final String typeName,
		final String    message           ,
		final Throwable cause             ,
		final boolean   enableSuppression ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.typeName = typeName;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters          //
	/////////////////////

	public String getTypeName()
	{
		return this.typeName;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public String getMessage()
	{
		return "Unresolvable dictionary type: \"" + this.typeName + "\"."
			+ (super.getMessage() != null ? " Details: " + super.getMessage() : "")
		;
	}



}
