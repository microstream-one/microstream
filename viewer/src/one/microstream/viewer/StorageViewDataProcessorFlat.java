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
 * Convert to flat data structures for later conversion into other formats
 * this implementation of StorageViewDataProcessor does not process children
 * of the input objects
 *
 */
public class StorageViewDataProcessorFlat implements StorageViewDataProcessor
{

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageViewDataProcessorFlat()
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
		obj.setType(description.getPersistenceTypeDefinition().typeName());
		obj.setMemberCount(description.getMembers().size());

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

		obj.setType(ViewerObjectReference.class.getTypeName());

		return obj;
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
			simpleMember.setValue(value);
		}
		else if (member instanceof ViewerObjectReference)
		{
			final ReferenceValue value = new ReferenceValue();
			value.setValue(Long.toString(((ViewerObjectReferenceWrapper)member.getValue()).getObjectId()));
			simpleMember.setValue(value);
		}
		else if(member instanceof ViewerObjectMemberList)
		{
			simpleMember.setMemberCount(member.getMembers().size());
		}
		else if(member instanceof ViewerObjectMemberComplexList)
		{
			simpleMember.setMemberCount(member.getMembers().size());
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
