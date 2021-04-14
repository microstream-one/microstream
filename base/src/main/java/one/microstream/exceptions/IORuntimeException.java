package one.microstream.exceptions;

import java.io.IOException;

public class IORuntimeException extends WrapperRuntimeException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public IORuntimeException(final IOException actual)
	{
		super(actual);
//		this.addSuppressed(actual);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public IOException getActual()
	{
		return (IOException)super.getActual(); // safe via constructor
	}



}

