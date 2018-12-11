package net.jadoth.exceptions;


/**
 *
 * @author Thomas Muenz
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
