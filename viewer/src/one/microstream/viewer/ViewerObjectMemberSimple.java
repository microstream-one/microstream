package one.microstream.viewer;

import java.util.ArrayList;
import java.util.List;

import one.microstream.persistence.types.PersistenceTypeDefinitionMember;

public class ViewerObjectMemberSimple extends ViewerObjectMemberDescription
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ViewerObjectMemberSimple(final PersistenceTypeDefinitionMember typeDefinition, final Object value)
	{
		super(typeDefinition, value);
	}


	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public List<ViewerObjectMemberDescription> getMembers()
	{
		final ArrayList<ViewerObjectMemberDescription> members = new ArrayList<>();
		return members;
	}


	@Override
	public ViewerObjectMemberDescription getMember(final int index)
	{
		return null;
	}
}
