package one.microstream.storage.restadapter;

public interface StorageRestAdapterObject extends StorageRestAdapterConverter
{
	public long getDefaultDataLength();

	public void setDefaultDataLength(long defaultDataLength);

	public ViewerObjectDescription getObject(long objectId, long dataOffset, long dataLength, long valueOffset,
			long valueLength, boolean resolveReferences, long referenceOffset, long referenceLength);
}