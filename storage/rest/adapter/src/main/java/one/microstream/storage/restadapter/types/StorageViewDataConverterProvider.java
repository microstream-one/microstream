package one.microstream.storage.restadapter.types;

public interface StorageViewDataConverterProvider
{
	/**
	 * Get the converter for the requested format.
	 *
	 * @param format
	 * @return StorageViewDataConverter
	 */
	public StorageViewDataConverter getConverter(String format);
}
