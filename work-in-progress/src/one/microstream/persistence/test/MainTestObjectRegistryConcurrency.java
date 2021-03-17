package one.microstream.persistence.test;

import static one.microstream.math.XMath.random;

import one.microstream.concurrency.XThreads;
import one.microstream.persistence.internal.DefaultObjectRegistry;
import one.microstream.typing.XTypes;

public class MainTestObjectRegistryConcurrency
{
	static final int   COUNT        = 1_000_000;
	static final float HASH_DENSITY = 100F     ; // provoke collisions
	static final int   DELAY        = 10       ;

	static final Object[] objects   = new Object[COUNT];
	static final long[]   objectIds = new long[COUNT];
	static final DefaultObjectRegistry reg = DefaultObjectRegistry.New(HASH_DENSITY);

	static
	{
		long objectId = 1_000_000_000_000L;
		for(int i = 0; i < objects.length; i++)
		{
			objectIds[i] = ++objectId;
			objects[i] = new Object();
			if(i % 2 == 0)
			{
				reg.registerObject(objectIds[i], objects[i]);
			}
		}
//		reg.DEBUG_analyze();
	}



	public static void main(final String[] args)
	{
		new Thread("lookup"){
			@Override public void run() {
				while(true)
				{
					final int index = random(COUNT);
					System.out.print("Looking up "+objectIds[index]+"... ");
					System.out.println(reg.lookupObject(objectIds[index]));

					System.out.print("Looking up "+objects[index]+"... ");
					System.out.println(reg.lookupObjectId(objects[index]));
//					JaThreads.sleep(DELAY);
				}
			}
		}.start();

		new Thread("register"){
			@Override public void run() {
				while(true)
				{
					final int index = random(COUNT);
					System.err.println("Registering "+objectIds[index]);
					reg.registerObject(objectIds[index], objects[index]);
					XThreads.sleep(DELAY<<3);
				}
			}
		}.start();

		new Thread("remove"){
			@Override public void run() {
				while(true)
				{
					final int index = random(COUNT);
					System.err.println("Removing "+objectIds[index]);
//					reg.removeObjectById(oids[index]);
					XThreads.sleep(DELAY<<3);
				}
			}
		}.start();

		new Thread("monitor"){
			@Override public void run() {
				while(true)
				{
					System.err.println(XTypes.to_int(reg.size()));
					XThreads.sleep(DELAY<<2);
				}
			}
		}.start();
	}

}
