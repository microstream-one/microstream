package one.microstream.exceptions;



/**
 *
 * 
 */
public class InstantiationRuntimeException extends WrapperRuntimeException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public InstantiationRuntimeException(final InstantiationException actual)
	{
		super(actual);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public InstantiationException getActual()
	{
		// cast safety guaranteed by constructor
		return (InstantiationException)super.getActual();
	}

}
