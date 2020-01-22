package one.microstream.viewer;

public interface StorageViewDataConverter
{
	String convert(Object object);
	String getHtmlResponseContentType();
}