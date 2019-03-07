package one.microstream.collections;

public class CapacityExceededException extends IndexExceededException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public CapacityExceededException()
	{
		super();
	}

	public CapacityExceededException(final String message)
	{
		super(message);
	}

	public CapacityExceededException(final int bound, final int index, final String message)
	{
		super(bound, index, message);
	}

	public CapacityExceededException(final int bound, final int index)
	{
		super(bound, index);
	}



}
