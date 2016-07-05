package net.jadoth.util.branching;

/**
 * @author Thomas Muenz
 *
 */
public abstract class AbstractBranchingThrow extends RuntimeException
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Object hint;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

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
	// getters          //
	/////////////////////

	public Object getHint()
	{
		return this.hint;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public synchronized AbstractBranchingThrow fillInStackTrace()
	{
		return this;
	}

}
