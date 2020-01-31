package one.microstream.storage.restadapter;

public interface StorageRestAdapterConverter
{
	public StorageViewDataConverter getConverter(String format);
}