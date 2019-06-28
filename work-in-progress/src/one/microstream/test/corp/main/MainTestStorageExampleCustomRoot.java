package one.microstream.test.corp.main;

import one.microstream.chars.XChars;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;


final class MyRoot
{
	long creationTime = System.currentTimeMillis();
	
	@Override
	public String toString()
	{
		return XChars.systemString(this) + ": " + this.creationTime;
	}
}

public class MainTestStorageExampleCustomRoot
{
	static final MyRoot ROOT = new MyRoot();
	
	

	public static void main(final String[] args)
	{
		System.out.println(ROOT);
		
		final EmbeddedStorageManager storage = EmbeddedStorage.start(ROOT);
		
		System.out.println(ROOT);
		System.out.println(storage.defaultRoot().get());
		// must print three times the same timestamp for the root instance to be handled correctly
		
		System.exit(0);
	}
	
}
