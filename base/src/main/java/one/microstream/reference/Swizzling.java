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
	
	public static final long notFoundId()
	{
		return -1L;
	}
	
	public static final boolean isNullId(final long objectId)
	{
		return objectId == nullId();
	}
	
	public static final boolean isFoundId(final long objectId)
	{
		return objectId >= nullId();
	}
	
	public static final boolean isNotFoundId(final long objectId)
	{
		return objectId < nullId();
	}
	
	public static final boolean isProperId(final long objectId)
	{
		return objectId > nullId();
	}
	
	public static final boolean isNotProperId(final long objectId)
	{
		return objectId <= nullId();
	}
	
	public static final long toUnmappedObjectId(final Object object)
	{
		return object == null
			? nullId()
			: notFoundId()
		;
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
