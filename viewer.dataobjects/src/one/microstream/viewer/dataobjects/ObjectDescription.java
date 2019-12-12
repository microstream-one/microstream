package one.microstream.viewer.dataobjects;

public class ObjectDescription
{
	private String ObjectId;
	private String typeName;
	private MemberDescription[] members;
	private int memberCount;

	public ObjectDescription()
	{
		super();
	}

	public String getObjectId()
	{
		return this.ObjectId;
	}

	public void setObjectId(final String objectId)
	{
		this.ObjectId = objectId;
	}

	public String getType()
	{
		return this.typeName;
	}

	public void setType(final String type)
	{
		this.typeName = type;
	}

	public MemberDescription[] getMembers()
	{
		return this.members;
	}

	public void setMembers(final MemberDescription[] members)
	{
		this.members = members;
	}

	public void setMemberCount(final int count)
	{
		this.memberCount = count;
	}

	public int getMemberCount()
	{
		return this.memberCount;
	}
}
