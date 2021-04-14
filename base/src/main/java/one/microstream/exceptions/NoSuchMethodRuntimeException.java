package one.microstream.exceptions;


/**
 *
 * 
 */
public class NoSuchMethodRuntimeException extends WrapperRuntimeException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public NoSuchMethodRuntimeException(final NoSuchMethodException actual)
	{
		super(actual);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public NoSuchMethodException getActual()
	{
		return (NoSuchMethodException)super.getActual(); // safe via constructor
	}

}
