package one.microstream.exceptions;

public class ParsingExceptionUnexpectedCharacter extends ParsingException
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final char expectedCharacter   ;
	private final char encounteredCharacter;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ParsingExceptionUnexpectedCharacter(
		final char expectedCharacter   ,
		final char encounteredCharacter
	)
	{
		super();
		this.expectedCharacter    = expectedCharacter   ;
		this.encounteredCharacter = encounteredCharacter;
	}

	public ParsingExceptionUnexpectedCharacter(
		final char      expectedCharacter   ,
		final char      encounteredCharacter,
		final Throwable cause
	)
	{
		super(cause);
		this.expectedCharacter    = expectedCharacter   ;
		this.encounteredCharacter = encounteredCharacter;
	}

	public ParsingExceptionUnexpectedCharacter(
		final char   expectedCharacter   ,
		final char   encounteredCharacter,
		final String message
	)
	{
		super(message);
		this.expectedCharacter    = expectedCharacter   ;
		this.encounteredCharacter = encounteredCharacter;
	}

	public ParsingExceptionUnexpectedCharacter(
		final char      expectedCharacter   ,
		final char      encounteredCharacter,
		final String    message             ,
		final Throwable cause
	)
	{
		super(message, cause);
		this.expectedCharacter    = expectedCharacter   ;
		this.encounteredCharacter = encounteredCharacter;
	}

	public ParsingExceptionUnexpectedCharacter(
		final char      expectedCharacter   ,
		final char      encounteredCharacter,
		final String    message             ,
		final Throwable cause               ,
		final boolean   enableSuppression   ,
		final boolean   writableStackTrace
	)
	{
		super(message, cause, enableSuppression, writableStackTrace);
		this.expectedCharacter    = expectedCharacter   ;
		this.encounteredCharacter = encounteredCharacter;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public final char expectedCharacter()
	{
		return this.expectedCharacter;
	}
	
	public final char encounteredCharacter()
	{
		return this.encounteredCharacter;
	}
	
	@Override
	public String assembleDetailString()
	{
		return "Encountered character '"
			+ this.encounteredCharacter
			+ "' is not the expected character '"
			+ this.expectedCharacter
			+ "'."
		;
	}
	
}
