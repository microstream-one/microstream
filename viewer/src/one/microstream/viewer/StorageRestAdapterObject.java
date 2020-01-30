package one.microstream.viewer;

public interface StorageRestAdapterObject extends StorageRestAdapterConverter
{
	public ViewerObjectDescription getObject(long objectId, long dataOffset, long dataLength, boolean resolveReferences,
			long referenceOffset, long referenceLength);

	public long getDefaultDataLength();

	public void setDefaultDataLength(long defaultDataLength);
}