package one.microstream.viewer;

import java.util.List;

import one.microstream.persistence.binary.types.ViewerObjectDescription;
import one.microstream.viewer.dataobjects.MemberDescription;
import one.microstream.viewer.dataobjects.ObjectDescription;
import one.microstream.viewer.dataobjects.RootObjectDescription;

/**
*
* This interface concentrates the complex informations from the ViewerObjectDescription
* to a simplified POJO data structures usable for exports to other formats like JSON
*
*/
public interface StorageViewDataProcessor
{

	ObjectDescription process(ViewerObjectDescription description);

	RootObjectDescription process(ViewerRootDescription description);

	MemberDescription process(ViewerObjectMemberDescription o);

	ObjectDescription process(ViewerObjectReference o);

	List<MemberDescription> processMemberList(List<ViewerObjectMemberDescription> members);

	List<RootObjectDescription> processRootList(List<ViewerRootDescription> roots);
}
