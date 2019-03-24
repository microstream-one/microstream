package one.microstream.util.traversing;


public final class TraversalSignalSkipEnqueueReference extends AbstractTraversalSkipSignal
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////
	
	// can be thrown any number of times, so a singleton instead of constant instantiation is the better approach
	static final TraversalSignalSkipEnqueueReference SINGLETON = new TraversalSignalSkipEnqueueReference();
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	/**
	 * Should actually be called "throw", but that is a keyword.
	 */
	public static void fire() throws TraversalSignalSkipEnqueueReference
	{
		throw SINGLETON;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	private TraversalSignalSkipEnqueueReference()
	{
		super();
	}
	
}
