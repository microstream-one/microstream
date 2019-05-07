package one.microstream.storage.exceptions;

public final class InsufficientBufferSpaceException extends Exception // intentionally checked exception
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final long requiredBufferSpace;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public InsufficientBufferSpaceException(final long requiredBufferSpace)
	{
		super();
		this.requiredBufferSpace = requiredBufferSpace;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public long requiredBufferSpace()
	{
		return this.requiredBufferSpace;
	}



}
