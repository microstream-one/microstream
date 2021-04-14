package one.microstream.exceptions;


/**
 *
 * 
 */
public class NoSuchFieldRuntimeException extends WrapperRuntimeException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public NoSuchFieldRuntimeException(final NoSuchFieldException actual)
	{
		super(actual);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public NoSuchFieldException getActual()
	{
		return (NoSuchFieldException)super.getActual(); // safe via constructor
	}

}
