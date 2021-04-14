
package one.microstream.storage.restclient.types;

import java.util.Map;

import one.microstream.persistence.types.PersistenceTypeDescription;
import one.microstream.storage.restadapter.types.ViewerObjectDescription;
import one.microstream.storage.restadapter.types.ViewerRootDescription;
import one.microstream.storage.restadapter.types.ViewerStorageFileStatistics;


public interface StorageRestClient extends AutoCloseable
{
	public Map<Long, PersistenceTypeDescription> requestTypeDictionary();
	
	public ViewerRootDescription requestRoot();
	
	public ViewerObjectDescription requestObject(
		ObjectRequest objectRequest
	);
	
	public ViewerStorageFileStatistics requestFileStatistics();
	
	@Override
	public void close();
	
}
