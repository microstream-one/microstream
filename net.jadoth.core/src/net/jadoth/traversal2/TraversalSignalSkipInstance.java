package net.jadoth.traversal2;


@Deprecated // (25.06.2017 TM)NOTE: should be obsolete with new concept
public final class TraversalSignalSkipInstance extends AbstractTraversalSignal
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////
	
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
