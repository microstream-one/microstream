package one.microstream.viewer;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import one.microstream.persistence.types.PersistenceTypeDefinitionMember;

public class ViewerObjectMemberList extends ViewerObjectMemberDescription
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ViewerObjectMemberList(final PersistenceTypeDefinitionMember typeDefinition, final Object value)
	{
		super(typeDefinition, value);
	}


	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public List<ViewerObjectMemberDescription> getMembers()
	{
		final int numElements =  Array.getLength(this.value);
		final List<ViewerObjectMemberDescription> membersDesc = new ArrayList<>();

		for(int i = 0; i < numElements; i++)
		{
			final Object mValue = Array.get(this.value, i);
			membersDesc.add(ViewerObjectMemberDescription.New(this.typeDefinition, mValue));
		}

		return membersDesc;
	}


	@Override
	public ViewerObjectMemberDescription getMember(final int index)
	{
		final Object mValue = Array.get(this.value, index);
		return ViewerObjectMemberDescription.New(this.typeDefinition, mValue);
	}
}
