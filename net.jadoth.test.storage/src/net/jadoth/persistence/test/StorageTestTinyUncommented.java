package net.jadoth.persistence.test;

import java.util.Date;

import net.jadoth.reference.Reference;
import net.jadoth.storage.types.EmbeddedStorage;
import net.jadoth.storage.types.EmbeddedStorageManager;
import net.jadoth.storage.types.Storage;


public class StorageTestTinyUncommented
{
	static final Reference<Object> ROOT = Reference.New(null);

	static final EmbeddedStorageManager STORAGE =
		EmbeddedStorage
		.createStorageManager(Storage.RootResolver(ROOT)/*, new File("c:/tinyTestStorage")/**/)
		.start()
	;

	public static void main(final String[] args)
	{
		ROOT.set(new Date());
		STORAGE.storeFull(ROOT);
		System.exit(0);
	}

}
