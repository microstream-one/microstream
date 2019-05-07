
package one.microstream.persistence.exceptions;

public class PersistenceExceptionTypeConsistencyDictionaryResolveFieldName
extends PersistenceExceptionTypeConsistencyDictionary
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Class<?> declaringType;
	private final String   fieldName    ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionTypeConsistencyDictionaryResolveFieldName(
		final Class<?> declaringType,
		final String fieldName
	)
	{
		this(declaringType, fieldName, null, null);
	}

	public PersistenceExceptionTypeConsistencyDictionaryResolveFieldName(
		final Class<?> declaringType,
		final String fieldName,
		final String message
	)
	{
		this(declaringType, fieldName, message, null);
	}

	public PersistenceExceptionTypeConsistencyDictionaryResolveFieldName(
		final Class<?> declaringType,
		final String fieldName,
		final Throwable cause
	)
	{
		this(declaringType, fieldName, null, cause);
	}

	public PersistenceExceptionTypeConsistencyDictionaryResolveFieldName(
		final Class<?> declaringType,
		final String fieldName,
		final String message, final Throwable cause
	)
	{
		this(declaringType, fieldName, message, cause, true, true);
	}

	public PersistenceExceptionTypeConsistencyDictionaryResolveFieldName(
		final Class<?> declaringType,
		final String fieldName,
		final String message,
		final Throwable cause,
		final boolean enableSuppression,
		final boolean writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.declaringType = declaringType;
		this.fieldName     = fieldName    ;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public Class<?> getDeclaringType()
	{
		return this.declaringType;
	}

	public String getTypeName()
	{
		return this.fieldName;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public String getMessage()
	{
		return "Unresolvable dictionary field \"" + this.declaringType.getName() + "#" + this.fieldName + "\"."
			+ (super.getMessage() != null ? " Details: " + super.getMessage() : "")
		;
	}



}
