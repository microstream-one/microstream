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
	ObjectDescription convertToObjectDescription(T from);

	T convert(RootObjectDescription rootObjectDescription);
	RootObjectDescription convertToRootObjectDescription(T json);

	T convert(MemberDescription preprocessed);
	T convert(List<MemberDescription> preprocessed);
}
