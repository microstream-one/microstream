package one.microstream.persistence.exceptions;

public class PersistenceExceptionParserMissingMemberType extends PersistenceExceptionParser
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public PersistenceExceptionParserMissingMemberType(
		final int index
	)
	{
		this(index, null, null);
	}

	public PersistenceExceptionParserMissingMemberType(
		final int index,
		final String message
	)
	{
		this(index, message, null);
	}

	public PersistenceExceptionParserMissingMemberType(
		final int index,
		final Throwable cause
	)
	{
		this(index, null, cause);
	}

	public PersistenceExceptionParserMissingMemberType(
		final int index,
		final String message, final Throwable cause
	)
	{
		this(index, message, cause, true, true);
	}

	public PersistenceExceptionParserMissingMemberType(
		final int index,
		final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace
	)
	{
		super(index, message, cause, enableSuppression, writableStackTrace);
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	@Override
	public String getMessage()
	{
		return "Missing member type at index " + this.getIndex() + "."
			+ (super.getMessage() != null ? " Details: " + super.getMessage() : "")
		;
	}
	
}
