package one.microstream.reference;

public final class Swizzling
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static final long nullId()
	{
		return 0L;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException
	 */
	private Swizzling()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
}
