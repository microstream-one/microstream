package net.jadoth.persistence.test;

import static net.jadoth.math.XMath.random;

import net.jadoth.concurrency.XThreads;
import net.jadoth.persistence.internal.DefaultObjectRegistry;
import net.jadoth.typing.XTypes;

@SuppressWarnings("deprecation")
public class MainTestObjectRegistryConcurrency
{
	static final int   COUNT        = 1_000_000;
	static final float HASH_DENSITY = 100F     ; // provoke collisions
	static final int   DELAY        = 10       ;

	static final Object[] objects = new Object[COUNT];
	static final long[]   oids    = new long[COUNT];
	static final DefaultObjectRegistry reg = DefaultObjectRegistry.New(HASH_DENSITY);
	
	static
	{
		long oid = 1_000_000_000_000L;
		for(int i = 0; i < objects.length; i++)
		{
			oids[i] = ++oid;
			objects[i] = new Object();
			if(i % 2 == 0)
			{
				reg.registerObject(oids[i], objects[i]);
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
					System.out.print("Looking up "+oids[index]+"... ");
					System.out.println(reg.lookupObject(oids[index]));

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
					System.err.println("Registering "+oids[index]);
					reg.registerObject(oids[index], objects[index]);
					XThreads.sleep(DELAY<<3);
				}
			}
		}.start();

		new Thread("remove"){
			@Override public void run() {
				while(true)
				{
					final int index = random(COUNT);
					System.err.println("Removing "+oids[index]);
					reg.removeObjectById(oids[index]);
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
