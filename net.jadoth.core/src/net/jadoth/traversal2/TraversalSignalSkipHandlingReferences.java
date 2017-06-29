package net.jadoth.traversal2;


public final class TraversalSignalSkipHandlingReferences extends AbstractTraversalSkipSignal
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////
	
	// can be thrown any number of times, so a singleton instead of constant instantiation is the better approach
	static final TraversalSignalSkipHandlingReferences SINGLETON = new TraversalSignalSkipHandlingReferences();
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	/**
	 * Should actually be called "throw", but that is a keyword.
	 */
	public static void fire() throws TraversalSignalSkipHandlingReferences
	{
		throw SINGLETON;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	private TraversalSignalSkipHandlingReferences()
	{
		super();
	}
	
}
