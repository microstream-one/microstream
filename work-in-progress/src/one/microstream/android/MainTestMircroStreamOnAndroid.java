package one.microstream.android;

import one.microstream.memory.android.MicroStreamAndroidAdapter;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;


public class MainTestMircroStreamOnAndroid
{
	static
	{
		MicroStreamAndroidAdapter.setupFull();
	}
	
	// creates and starts an embedded storage manager with all-default-settings.
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage.start();

	public static void main(final String[] args)
	{
		// do stuff
	}
		
}
