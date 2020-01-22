package one.microstream.viewer;

public interface StorageViewDataConverter
{
	String convert(ViewerRootDescription root);
	String convert(ViewerObjectDescription preprocessed);
	String getHtmlResponseContentType();
}