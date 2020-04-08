package one.microstream.storage.restadapter;

public interface StorageViewDataConverter
{
	public String convert(Object object);
	
	public String getHtmlResponseContentType();
	
	public String[] getFormatStrings();
}
