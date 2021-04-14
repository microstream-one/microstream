package one.microstream.storage.restadapter.types;

public interface StorageRestAdapterObject extends StorageViewDataConverterProvider
{
	public long getDefaultValueLength();

	public void setDefaultValueLength(long defaultValueLength);

	public ViewerObjectDescription getObject(long objectId, long fixedOffset, long fixedLength, long variableOffset,
			long variableLength, long valueLength, boolean resolveReferences);
}