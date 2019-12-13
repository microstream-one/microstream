package one.microstream.viewer;

import one.microstream.persistence.binary.types.ViewerObjectDescription;
import one.microstream.persistence.binary.types.ViewerObjectReferenceWrapper;
import one.microstream.viewer.dataobjects.MemberDescription;
import one.microstream.viewer.dataobjects.MemberValue;
import one.microstream.viewer.dataobjects.ObjectDescription;
import one.microstream.viewer.dataobjects.ReferenceValue;

/**
 *
 * Convert to flat data structures for later conversion into other formats
 * this implementation of StorageViewDataProcessor does not process children
 * of the input objects
 *
 */
public class StorageViewDataProcessorFlat extends StorageViewDataProcessorBase
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
		obj.setTypeName(description.getPersistenceTypeDefinition().typeName());
		obj.setMemberCount(description.getMembers().size());
		obj.setNativeValue(description.getPrimitiveInstance());

		return obj;
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
		}
		else if(member instanceof ViewerObjectMemberComplexList)
		{
			memberDescription.setMemberCount(member.getMembers().size());
		}

		return memberDescription;
	}
}
