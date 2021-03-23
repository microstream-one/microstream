package one.microstream.test;

import java.time.ZoneId;

import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;

public class MainTestZoneId
{
	public static void main(final String[] args)
	{
		final EmbeddedStorageManager storageManager = EmbeddedStorage.start();
		Object root = storageManager.root();
		if(root == null)
		{
			storageManager.setRoot(root = new Root());
			storageManager.storeRoot();
		}
		System.out.println(root);
		storageManager.shutdown();
		System.exit(0);
	}

	static class Root
	{
		final ZoneId region = ZoneId.of("CET"); // creates a ZoneRegion
		final ZoneId offset = ZoneId.of("+1");  // creates a ZoneOffset

		@Override
		public String toString()
		{
			return "Root [region=" + this.region + ", offset=" + this.offset + "]";
		}
	}

}
