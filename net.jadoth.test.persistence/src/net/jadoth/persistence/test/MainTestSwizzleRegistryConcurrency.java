package net.jadoth.persistence.test;

import static net.jadoth.math.JadothMath.random;

import net.jadoth.concurrent.JadothThreads;
import net.jadoth.swizzling.internal.SwizzleRegistryGrowingRange;
import net.jadoth.util.JadothTypes;

@SuppressWarnings("deprecation")
public class MainTestSwizzleRegistryConcurrency
{
	static final int   COUNT        = 1_000_000;
	static final float HASH_DENSITY = 100F     ; // provoke collisions
	static final int   DELAY        = 10       ;

	static final Object[] objects = new Object[COUNT];
	static final long[]   oids    = new long[COUNT];
	static final SwizzleRegistryGrowingRange reg = new SwizzleRegistryGrowingRange(HASH_DENSITY);
	static {
		reg.registerType(10L, Object.class);
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
		reg.DEBUG_analyze();
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
					JadothThreads.sleep(DELAY<<3);
				}
			}
		}.start();

		new Thread("remove"){
			@Override public void run() {
				while(true)
				{
					final int index = random(COUNT);
					System.err.println("Removing "+oids[index]);
					reg.removeById(oids[index]);
					JadothThreads.sleep(DELAY<<3);
				}
			}
		}.start();

		new Thread("monitor"){
			@Override public void run() {
				while(true)
				{
					System.err.println(JadothTypes.to_int(reg.size()));
					JadothThreads.sleep(DELAY<<2);
				}
			}
		}.start();
	}

}
