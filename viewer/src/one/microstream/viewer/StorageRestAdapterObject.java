package one.microstream.viewer;

public interface StorageRestAdapterObject extends StorageRestAdapterConverter
{
	ViewerObjectDescription getObject(long objectId, long dataOffset, long dataLength, boolean resolveReferences,
			long referenceOffset, long referenceLength);

}