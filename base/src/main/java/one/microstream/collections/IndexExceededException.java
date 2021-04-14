package one.microstream.collections;

import one.microstream.chars.VarString;

/**
 * Why is it that one has to write every piece of the JDK in "proper" oneself?
 * Providing the essential describing values of a problem in a structed form is essential
 * for both programmatic and manual processing, not a personal flavor.
 *
 * 
 */
public class IndexExceededException extends IndexOutOfBoundsException
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final long bound;
	private final long index;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public IndexExceededException(final long bound, final long index)
	{
		super();
		this.bound = bound;
		this.index = index;
	}

	public IndexExceededException(final long bound, final long index, final String message)
	{
		super(message);
		this.bound = bound;
		this.index = index;
	}

	public IndexExceededException()
	{
		this(0, 0);
	}

	public IndexExceededException(final String message)
	{
		this(0, 0, message); // it's MESSAGE. Not just "s". FFS! That "Yellin" guy has the right name.
	}



	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public long index()
	{
		return this.index;
	}

	public long bound()
	{
		return this.bound;
	}

	@Override
	public String getMessage()
	{
		final VarString vc = VarString.New()
			.add("Index = ").add(this.index).add(", bound = ").add(this.bound).add('.')
		;
		final String message = super.getMessage();
		if(message != null)
		{
			vc.add(' ').add(message);
		}
		return vc.toString();
	}



}
