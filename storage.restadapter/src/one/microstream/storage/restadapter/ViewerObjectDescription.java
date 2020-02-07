package one.microstream.storage.restadapter;

public class ViewerObjectDescription
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private String objectId;
	private String typeId;
	private String length;
	private Object[] data;
	private ViewerObjectDescription[] references;


	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ViewerObjectDescription()
	{
		super();
	}


	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	public String getObjectId()
	{
		return this.objectId;
	}

	public void setObjectId(final String objectId)
	{
		this.objectId = objectId;
	}

	public String getTypeId()
	{
		return this.typeId;
	}

	public void setTypeId(final String typeId)
	{
		this.typeId = typeId;
	}

	public String getLength()
	{
		return this.length;
	}

	public void setLength(final String length)
	{
		this.length = length;
	}

	public Object[] getData()
	{
		return this.data;
	}

	public void setData(final Object[] data)
	{
		this.data = data;
	}

	public ViewerObjectDescription[] getReferences()
	{
		return this.references;
	}

	public void setReferences(final ViewerObjectDescription[] references)
	{
		this.references = references;
	}



}
