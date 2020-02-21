package one.microstream.storage.restadapter;

public interface StorageRestAdapterObject extends StorageRestAdapterConverter
{
	public long getDefaultValueLength();

	public void setDefaultValueLength(long defaultValueLength);

	public ViewerObjectDescription getObject(long objectId, long fixedOffset, long fixedLength, long variableOffset,
			long variableLength, long referenceOffset, long referenceLength, long valueLength,
			boolean resolveReferences);
}