package one.microstream.exceptions;


/**
 *
 * 
 */
public class IllegalAccessRuntimeException extends WrapperRuntimeException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public IllegalAccessRuntimeException(final IllegalAccessException actual)
	{
		super(actual);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public IllegalAccessException getActual()
	{
		return (IllegalAccessException)super.getActual(); // safe via constructor
	}

}
