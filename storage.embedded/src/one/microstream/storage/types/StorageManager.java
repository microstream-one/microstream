package one.microstream.storage.types;

import one.microstream.persistence.types.PersistenceRootsView;
import one.microstream.reference.Reference;

public interface StorageManager extends StorageController, StorageConnection
{
	public StorageTypeDictionary typeDictionary();

	public StorageConnection createConnection();

	public StorageConfiguration configuration();

	public void initialize();

	@Override
	public StorageManager start();

	@Override
	public boolean shutdown();
	
	/**
	 * @return the persistent object graph's root object.
	 */
	public Object root();
	
	public Object setRoot(Object newRoot);
	
	/**
	 * This method is deprecated due to simplified root handling and will be removed in a future version.<br>
	 * It is advised to use {@link #root()} and {@link #setRoot(Object)} instead.
	 * 
	 * @deprecated
	 * 
	 * @return a mutable {@link Reference} to the root object.
	 */
	@Deprecated
	public Reference<Object> defaultRoot();

	/**
	 * This method is deprecated due to simplified root handling and will be removed in a future version.<br>
	 * It is advised to use {@link #root()} instead, for which this method is an alias.
	 * 
	 * @deprecated
	 * 
	 * @return the root object.
	 */
	@Deprecated
	public default Object customRoot()
	{
		return this.root();
	}
	
	public PersistenceRootsView viewRoots();
	
	public long storeRoot();
	
	/**
	 * This method is deprecated due to simplified root handling and will be removed in a future version.<br>
	 * It is advised to use {@link #storeRoot()} instead, for which this method is an alias.
	 * 
	 * @deprecated
	 * 
	 * @return stores the root object and returns its objectId.
	 */
	@Deprecated
	public default long storeDefaultRoot()
	{
		return this.storeRoot();
	}

}
