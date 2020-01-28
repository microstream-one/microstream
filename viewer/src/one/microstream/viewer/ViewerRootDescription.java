package one.microstream.viewer;

public class ViewerRootDescription
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private String name;
	private long objectId;


	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ViewerRootDescription()
	{
		super();
	}

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


	public void setName(final String name) {
		this.name = name;
	}


	public void setObjectId(final long objectId) {
		this.objectId = objectId;
	}


}
