package one.microstream.persistence.binary.types;

import java.util.List;

import one.microstream.viewer.ViewerObjectMemberDescription;

public interface ViewerMemberProvider
{
	/**
	 * Get a list of all members of this object
	 *
	 * @return ViewerObjectMemberDescription array
	 */
	List<ViewerObjectMemberDescription> getMembers();
	List<ViewerObjectMemberDescription> getMembers(int offset, int limit);
	ViewerObjectMemberDescription       getMember(int index);
}