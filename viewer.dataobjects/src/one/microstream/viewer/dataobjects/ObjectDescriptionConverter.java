package one.microstream.viewer.dataobjects;

import java.util.List;

/**
 *
 * Interface for data converter from microstream
 * viewer data to other formats
 *
 * @param <T>
 */
public interface ObjectDescriptionConverter<T>
{
	T convert (ObjectDescription objectDescription);
	ObjectDescription toObjectDescription(T from);

	T convert(RootObjectDescription rootObjectDescription);
	RootObjectDescription toRootObjectDescription(T from);

	T convert(MemberDescription preprocessed);
	MemberDescription toMemberDescription(T from);

	T convert(List<MemberDescription> preprocessed);
	List<MemberDescription> toMemberList(T from);
}
