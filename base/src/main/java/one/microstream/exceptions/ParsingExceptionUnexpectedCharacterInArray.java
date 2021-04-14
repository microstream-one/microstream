package one.microstream.exceptions;

public class ParsingExceptionUnexpectedCharacterInArray extends ParsingExceptionUnexpectedCharacter
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final char[] array;
	private final int    index;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ParsingExceptionUnexpectedCharacterInArray(
		final char[] array               ,
		final int    index               ,
		final char   expectedCharacter   ,
		final char   encounteredCharacter
	)
	{
		super(expectedCharacter, encounteredCharacter);
		this.array = array;
		this.index = index;
	}

	public ParsingExceptionUnexpectedCharacterInArray(
		final char[]    array               ,
		final int       index               ,
		final char      expectedCharacter   ,
		final char      encounteredCharacter,
		final Throwable cause
	)
	{
		super(expectedCharacter, encounteredCharacter, cause);
		this.array = array;
		this.index = index;
	}

	public ParsingExceptionUnexpectedCharacterInArray(
		final char[] array               ,
		final int    index               ,
		final char   expectedCharacter   ,
		final char   encounteredCharacter,
		final String message
	)
	{
		super(expectedCharacter, encounteredCharacter, message);
		this.array = array;
		this.index = index;
	}

	public ParsingExceptionUnexpectedCharacterInArray(
		final char[]    array               ,
		final int       index               ,
		final char      expectedCharacter   ,
		final char      encounteredCharacter,
		final String    message             ,
		final Throwable cause
	)
	{
		super(expectedCharacter, encounteredCharacter, message, cause);
		this.array = array;
		this.index = index;
	}

	public ParsingExceptionUnexpectedCharacterInArray(
		final char[]    array               ,
		final int       index               ,
		final char      expectedCharacter   ,
		final char      encounteredCharacter,
		final String    message             ,
		final Throwable cause               ,
		final boolean   enableSuppression   ,
		final boolean   writableStackTrace
	)
	{
		super(expectedCharacter, encounteredCharacter, message, cause, enableSuppression, writableStackTrace);
		this.array = array;
		this.index = index;
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public final char[] array()
	{
		return this.array;
	}
	
	public final int index()
	{
		return this.index;
	}
	
	@Override
	public String assembleDetailString()
	{
		return "Problem at index " + this.index + ": " + super.assembleDetailString();
	}
	
}
