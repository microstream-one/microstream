/**
 * 
 */
package net.jadoth.util.branching;

/**
 * @author Thomas Muenz
 *
 */
public class BranchingThrowInvalidException extends RuntimeException
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BranchingThrowInvalidException()
	{
		super();
	}

	public BranchingThrowInvalidException(final String message)
	{
		super(message);
	}

	public BranchingThrowInvalidException(final AbstractBranchingThrow cause)
	{
		super(cause);
	}

	public BranchingThrowInvalidException(final String message, final AbstractBranchingThrow cause)
	{
		super(message, cause);
	}
	
}
