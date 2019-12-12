package one.microstream.viewer;

public class ViewerRootDescription
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final String name;
	private final long objectId;


	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ViewerRootDescription(final String name, final long objectId)
	{
		super();

		this.name = name;
		this.objectId = objectId;
	}


	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	public String getName()
	{
		return this.name;
	}

	public long getObjectId()
	{
		return this.objectId;
	}
}
