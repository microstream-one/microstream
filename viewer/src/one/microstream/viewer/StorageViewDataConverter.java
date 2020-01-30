package one.microstream.viewer;

public interface StorageViewDataConverter
{
	public String convert(Object object);

	public String getHtmlResponseContentType();
}