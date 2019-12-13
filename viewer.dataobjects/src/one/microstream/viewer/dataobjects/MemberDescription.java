package one.microstream.viewer.dataobjects;

public class MemberDescription
{
	private String name;
	private String typeName;
	MemberValue memberValue;
	MemberDescription[] members;
	private int memberCount;

	public MemberDescription()
	{
		super();
	}

	public String getName()
	{
		return this.name;
	}

	public void setName(final String name)
	{
		this.name = name;
	}

	public String getTypeName()
	{
		return this.typeName;
	}

	public void setType(final String typeName)
	{
		this.typeName = typeName;
	}

	public MemberValue getMemberValue()
	{
		return this.memberValue;
	}

	public void setMemberValue(final MemberValue memberValue)
	{
		this.memberValue = memberValue;
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
