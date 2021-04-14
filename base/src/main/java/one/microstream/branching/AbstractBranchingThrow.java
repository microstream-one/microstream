package one.microstream.branching;

/**
 * 
 *
 */
public abstract class AbstractBranchingThrow extends RuntimeException
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Object hint;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected AbstractBranchingThrow()
	{
		super();
		this.hint = null;
	}

	protected AbstractBranchingThrow(final Throwable cause)
	{
		super(cause);
		this.hint = null;
	}

	protected AbstractBranchingThrow(final Object hint)
	{
		super();
		this.hint = hint;
	}

	protected AbstractBranchingThrow(final Object hint, final Throwable cause)
	{
		super(cause);
		this.hint = hint;
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	public Object getHint()
	{
		return this.hint;
	}
	
	@Override
	public synchronized AbstractBranchingThrow fillInStackTrace()
	{
		return this;
	}

}
