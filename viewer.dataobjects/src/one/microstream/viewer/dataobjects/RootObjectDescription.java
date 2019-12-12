package one.microstream.viewer.dataobjects;

public class RootObjectDescription
{
	private String name;
	private String objectId;

	public RootObjectDescription()
	{
		super();
	}

	public RootObjectDescription(final String name, final String objectId)
	{
		super();
		this.name = name;
		this.objectId = objectId;
	}

	public String getName()
	{
		return this.name;
	}

	public void setName(final String name)
	{
		this.name = name;
	}

	public String getObjectId()
	{
		return this.objectId;
	}

	public void setObjectId(final String objectId)
	{
		this.objectId = objectId;
	}


}
