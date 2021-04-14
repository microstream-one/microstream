package one.microstream.exceptions;

import java.io.InvalidClassException;


/**
 *
 * 
 */
public class InvalidClassRuntimeException extends WrapperRuntimeException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public InvalidClassRuntimeException(final InvalidClassException actual)
	{
		super(actual);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public InvalidClassException getActual()
	{
		return (InvalidClassException)super.getActual(); // safe via constructor
	}

}
