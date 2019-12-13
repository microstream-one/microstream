package one.microstream.viewer;

import java.util.ArrayList;
import java.util.List;

import one.microstream.viewer.dataobjects.MemberDescription;
import one.microstream.viewer.dataobjects.ObjectDescription;
import one.microstream.viewer.dataobjects.RootObjectDescription;


/**
*
* Basic implementation to convert the complex informations from the ViewerObjectDescription
* to a simplified POJO data structures usable for exports to other formats like JSON
*
*/

public abstract class StorageViewDataProcessorBase implements StorageViewDataProcessor
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public StorageViewDataProcessorBase()
	{
		super();
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public RootObjectDescription process(final ViewerRootDescription description)
	{
		return new RootObjectDescription(description.getName(), Long.toString(description.getObjectId()));
	}

	@Override
	public ObjectDescription process(final ViewerObjectReference o)
	{
		final ObjectDescription obj = new ObjectDescription();

		obj.setTypeName(ViewerObjectReference.class.getTypeName());

		return obj;
	}

	@Override
	public List<MemberDescription> processMemberList(final List<ViewerObjectMemberDescription> members)
	{
		final List<MemberDescription> descriptions = new ArrayList<>(members.size());

		for (final ViewerObjectMemberDescription memberDescription : members)
		{
			descriptions.add(this.process(memberDescription));
		}

		return descriptions;
	}

	@Override
	public List<RootObjectDescription> processRootList(final List<ViewerRootDescription> roots)
	{
		final List<RootObjectDescription> descriptions = new ArrayList<>(roots.size());

		for (final ViewerRootDescription rootDescription : roots)
		{
			descriptions.add(new RootObjectDescription(rootDescription.getName(), Long.toString(rootDescription.getObjectId())));
		}

		return descriptions;
	}
}
