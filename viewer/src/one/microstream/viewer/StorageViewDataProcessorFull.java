package one.microstream.viewer;

import java.util.ArrayList;
import java.util.List;

import one.microstream.persistence.binary.types.ViewerObjectDescription;
import one.microstream.persistence.binary.types.ViewerObjectReferenceWrapper;
import one.microstream.viewer.dataobjects.MemberDescription;
import one.microstream.viewer.dataobjects.MemberValue;
import one.microstream.viewer.dataobjects.ObjectDescription;
import one.microstream.viewer.dataobjects.ReferenceValue;


/**
*
* Convert to hierarchical data structure for later conversion into other formats
* this implementation of StorageViewDataProcessor also handles children
* of the input objects
*
*/
public class StorageViewDataProcessorFull extends StorageViewDataProcessorBase
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
		obj.setMembers(this.processMembers(description.getMembers()));
		obj.setNativeValue(description.getPrimitiveInstance());

		return obj;
	}

	private MemberDescription[] processMembers(final List<ViewerObjectMemberDescription> members)
	{
		final List<MemberDescription> simplifiedMembers = new ArrayList<>();

		for (final ViewerObjectMemberDescription member : members)
		{
			simplifiedMembers.add(this.process(member));
		}

		return simplifiedMembers.toArray(new MemberDescription[0]);
	}

	@Override
	public MemberDescription process(final ViewerObjectMemberDescription member)
	{
		final MemberDescription memberDescription = new MemberDescription();

		memberDescription.setName(member.getName());
		memberDescription.setType(member.getTypeName());

		if (member instanceof ViewerObjectMemberSimple)
		{
			final MemberValue value = new MemberValue();
			value.setValue(member.getValue().toString());
			memberDescription.setMemberValue(value);
		}
		else if (member instanceof ViewerObjectReference)
		{
			final ReferenceValue value = new ReferenceValue();
			value.setValue(Long.toString(((ViewerObjectReferenceWrapper)member.getValue()).getObjectId()));
			memberDescription.setMemberValue(value);
		}
		else if(member instanceof ViewerObjectMemberList)
		{
			memberDescription.setMemberCount(member.getMembers().size());
			memberDescription.setMembers(this.processMembers(member.getMembers()));
		}
		else if(member instanceof ViewerObjectMemberComplexList)
		{
			memberDescription.setMemberCount(member.getMembers().size());
			memberDescription.setMembers(this.processMembers(member.getMembers()));
		}

		return memberDescription;
	}
}
