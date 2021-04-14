package one.microstream.util.traversing;


public final class TraversalSignalSkipInstance extends AbstractTraversalSkipSignal
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////
	
	// can be thrown any number of times, so a singleton instead of constant instantiation is the better approach
	static final TraversalSignalSkipInstance SINGLETON = new TraversalSignalSkipInstance();
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	/**
	 * Should actually be called "throw", but that is a keyword.
	 */
	public static void fire() throws TraversalSignalSkipInstance
	{
		throw SINGLETON;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	private TraversalSignalSkipInstance()
	{
		super();
	}
	
}
