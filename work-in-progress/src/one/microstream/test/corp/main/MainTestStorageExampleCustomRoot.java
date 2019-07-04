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
		System.out.println("Pre -start() custom root state: " + ROOT);
		
		final EmbeddedStorageManager storage = EmbeddedStorage.start(ROOT);
		
		System.out.println("Post-start() custom root state: " + ROOT);
		System.out.println("Post-start() default root     : " + storage.defaultRoot());
		/*
		 * Must be:
		 * 1.) First run
		 * - some timestamp
		 * - same timestamp again
		 * - null as default root (no default root is created in the first place)
		 * 
		 * 2.) Second run
		 * - new timestamp
		 * - previous timestamp
		 * - null as default root (no default root is created in the first place)
		 */
		
		System.exit(0);
	}
	
}
