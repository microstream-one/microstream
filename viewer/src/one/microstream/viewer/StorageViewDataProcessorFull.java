package one.microstream.viewer;

import java.util.ArrayList;
import java.util.List;

import one.microstream.persistence.binary.types.ViewerObjectDescription;
import one.microstream.persistence.binary.types.ViewerObjectReferenceWrapper;
import one.microstream.viewer.dataobjects.MemberDescription;
import one.microstream.viewer.dataobjects.MemberValue;
import one.microstream.viewer.dataobjects.ObjectDescription;
import one.microstream.viewer.dataobjects.ReferenceValue;
import one.microstream.viewer.dataobjects.RootObjectDescription;


/**
*
* Convert to hierarchical data structure for later conversion into other formats
* this implementation of StorageViewDataProcessor also handles children
* of the input objects
*
*/
public class StorageViewDataProcessorFull implements StorageViewDataProcessor
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageViewDataProcessorFull()
	{
		super();
	}


	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public ObjectDescription process(final ViewerObjectDescription description)
	{
		final ObjectDescription obj = new ObjectDescription();

		obj.setObjectId(Long.toString(description.getObjectId()));
		obj.setTypeName(description.getPersistenceTypeDefinition().typeName());
		obj.setMemberCount(description.getMembers().size());
		obj.setMembers(this.simplifyMembers(description.getMembers()));
		obj.setNativeValue(description.getPrimitiveInstance());

		return obj;
	}

	@Override
	public RootObjectDescription process(final ViewerRootDescription description)
	{
		return new RootObjectDescription(description.getName(), Long.toString(description.getObjectId()));
	}

	@Override
	public MemberDescription process(final ViewerObjectMemberDescription o)
	{
		return this.simplifiyMember(o);
	}

	@Override
	public ObjectDescription process(final ViewerObjectReference o)
	{
		final ObjectDescription obj = new ObjectDescription();

		obj.setTypeName(ViewerObjectReference.class.getTypeName());

		return obj;
	}

	private MemberDescription[] simplifyMembers(final List<ViewerObjectMemberDescription> members)
	{
		final List<MemberDescription> simplifiedMembers = new ArrayList<>();

		for (final ViewerObjectMemberDescription member : members)
		{
			simplifiedMembers.add(this.simplifiyMember(member));
		}

		return simplifiedMembers.toArray(new MemberDescription[0]);
	}

	private MemberDescription simplifiyMember(final ViewerObjectMemberDescription member)
	{
		final MemberDescription simpleMember = new MemberDescription();

		simpleMember.setName(member.getName());
		simpleMember.setType(member.getTypeName());

		if (member instanceof ViewerObjectMemberSimple)
		{
			final MemberValue value = new MemberValue();
			value.setValue(member.getValue().toString());
			simpleMember.setMemberValue(value);
		}
		else if (member instanceof ViewerObjectReference)
		{
			final ReferenceValue value = new ReferenceValue();
			value.setValue(Long.toString(((ViewerObjectReferenceWrapper)member.getValue()).getObjectId()));
			simpleMember.setMemberValue(value);
		}
		else if(member instanceof ViewerObjectMemberList)
		{
			simpleMember.setMemberCount(member.getMembers().size());
			simpleMember.setMembers(this.simplifyMembers(member.getMembers()));
		}
		else if(member instanceof ViewerObjectMemberComplexList)
		{
			simpleMember.setMemberCount(member.getMembers().size());
			simpleMember.setMembers(this.simplifyMembers(member.getMembers()));
		}

		return simpleMember;
	}

	@Override
	public List<MemberDescription> process(final List<ViewerObjectMemberDescription> members)
	{
		final List<MemberDescription> descriptions = new ArrayList<>(members.size());

		for (final ViewerObjectMemberDescription memberDescription : members)
		{
			descriptions.add(this.simplifiyMember(memberDescription));
		}

		return descriptions;
	}

}
