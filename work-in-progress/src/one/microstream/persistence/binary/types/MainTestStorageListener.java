
package one.microstream.persistence.binary.types;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageFoundation;
import one.microstream.storage.types.EmbeddedStorageManager;


public class MainTestStorageListener
{
	public static void main(
		final String[] args
	)
	{
		final StorageListener listener = new StorageListener()
		{
			@Override
			public void objectStoredLazy(
				final long objectId,
				final Object instance
			)
			{
				// change for custom logging
				StorageListener.super.objectStoredLazy(objectId, instance);
			}
			
			@Override
			public void objectStoredEager(
				final long objectId,
				final Object instance
			)
			{
				// change for custom logging
				StorageListener.super.objectStoredEager(objectId, instance);
			}
		};
		
		final EmbeddedStorageFoundation<?> foundation = EmbeddedStorage.Foundation();
		foundation.onConnectionFoundation(cf ->
		{
			cf.setStorerCreator(new ListeningBinaryStorerCreator(
				cf.getStorageSystem().
				channelCountProvider(),
				cf.isByteOrderMismatch(),
				listener
			));
		});
		
		final EmbeddedStorageManager storage = foundation.createEmbeddedStorageManager().start();
		
		@SuppressWarnings("unchecked")
		List<LocalDateTime> root = (List<LocalDateTime>)storage.root();
		if(root == null)
		{
			storage.setRoot(root = new ArrayList<>());
			storage.storeRoot();
		}
		root.add(LocalDateTime.now());
		storage.store(root);
		storage.shutdown();
	}
		
}
