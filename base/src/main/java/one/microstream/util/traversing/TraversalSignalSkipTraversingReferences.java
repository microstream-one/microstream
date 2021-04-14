package one.microstream.util.traversing;


public final class TraversalSignalSkipTraversingReferences extends AbstractTraversalSkipSignal
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////
	
	// can be thrown any number of times, so a singleton instead of constant instantiation is the better approach
	static final TraversalSignalSkipTraversingReferences SINGLETON = new TraversalSignalSkipTraversingReferences();
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	/**
	 * Should actually be called "throw", but that is a keyword.
	 */
	public static void fire() throws TraversalSignalSkipTraversingReferences
	{
		throw SINGLETON;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	private TraversalSignalSkipTraversingReferences()
	{
		super();
	}
	
}
