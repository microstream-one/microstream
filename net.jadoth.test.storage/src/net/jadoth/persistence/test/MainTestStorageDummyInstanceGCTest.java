package net.jadoth.persistence.test;

import java.io.File;
import java.util.Date;

import net.jadoth.concurrent.JadothThreads;
import net.jadoth.persistence.types.Storer;
import net.jadoth.storage.types.DEBUGStorage;
import net.jadoth.storage.types.StorageConnection;
import net.jadoth.swizzling.types.Lazy;
import net.jadoth.util.UtilResetDirectory;


/*
 * Test to investigate why/how (really if) initial dummy instances that get replace afterwards
 * cause the replacement instance to not be covered by GC and erroneously deleted instead.
 *
 * Original situation in production code (4 channels):
 * One business entity instance intially has "Lazy<...> field = Lazy.Reference(null);"
 * That "dummy instance" gets stored along with the business entity instance.
 * Then the business logic replaces that dummy lazy instance with an actual lazy instance pointing to a sub graph.
 * The entity instance gets stored again and with it the new lazy instance with its referenced sub graph.
 * The initial dummy instance gets collected, which is correct.
 * However, the newly referenced and stored sub graph entities get collected as well. 100% of the time.
 * Which is an error.
 * The GC encounters zombie references from then on.
 * Restarting the process throws an oid not found exception while attempting to load.
 * So they are really deleted erroneously.
 * There are never any older instances deleted, only those newly stored via the replacement lazy instances.
 *
 * Removing the initial dummy instances causes the error to go away.
 * So it must have something to do with instances replacing former instances in the same field of a re-stored instance.
 *
 * I have already checked every relevant point in the source code and everything looks correct.
 * So a problem like that should not be able to occur at all. And yet it does.
 *
 * Tried to reproduce that here.
 * With no success.
 *
 * However:
 * Using 4 channels, after always exactely 4 GC cycles the old date instance from the previous generation
 * no longer gets collected as it should be (and is in the first 4 cycles).
 * One channel explicitely collects only 2499 instances instead of 2500 as it should.
 * That is a bug.
 * Maybe the same bug causing the erroneous collection of reachable entities.
 * Or maybe another bug, that causes unreachable entities to remain uncollected
 * (already observed reproducably in production)
 *
 * Using 2 channels it always occurs after 2 GC cycles.
 * Using 8 channels after 8 GC cycles.
 *
 * This is due to the OID incrementation, every iteration of the store loop puts the date instance
 * into another channel. E.g. 2, 3, 4, 5, 6, 7, 0, 1
 * Maybe something gets "stuck" in the channel local type meta structure or something like that
 *
 * For 1 channel, it never occurs.
 *
 * debug-printlns showed:
 * After the provlem struck, no Date instance ever gets marked again. Neither the new ones nor the old ones.
 * Everyting stays marked as it was and gets safed by the sweep every time.
 * This CANNOT be as the sweep resets the GC state after marking, but still: yet it is.
 * 
 * If the graph is reduced to 1/1/1 length, the date gets collected correctly every time.
 * Only if multiple entities reference the date instance, it stays alive somehow
 *
 */
public class MainTestStorageDummyInstanceGCTest extends TestStorage
{
	static final int  RUNS      = 10000;
	static final long WAIT_TIME = 4000;

	public static void main(final String[] args)
	{
		deleteOutput();
		STORAGE.start();
		
//		doit();
		doitSimple();
		exit();
	}
	
	static void doit()
	{
		final Object[][][] data = generateGraph(4);
		ROOT.set(data);

		final StorageConnection connection = STORAGE.createConnection();
		connection.storeRequired(ROOT);

		for(int i = 0; i < RUNS; i++)
		{
			final Date now = new Date();

			DEBUGStorage.println("#" + i + " storing @" + now.getTime());

			final Storer storer = connection.createStorer();

			for(int i1 = 0; i1 < data.length; i1++)
			{
				final Object[][] e2 = data[i1];

				for(int i2 = 0; i2 < e2.length; i2++)
				{
					final Object[] e3 = e2[i2];

					for(int i3 = 0; i3 < e3.length; i3++)
					{
						((TestEntity)e3[i3]).setDate(now);
						storer.storeRequired(e3[i3]);
					}
				}
			}

			final long dateOid = connection.persistenceManager().lookupObjectId(now);

			DEBUGStorage.println("#" + i + " storing " + storer.size());

			storer.commit();

			DEBUGStorage.println("#" + i + " stored." + " (" + dateOid + ")");

			JadothThreads.sleep(WAIT_TIME);
		}
	}
	
	
	/* (25.08.2016)NOTE:
	 * 
	 *  For channelCount = 2, this collects a replaced date exactely two times (one for each channel),
	 *  e.g.:
	 *  0 Collecting 1000000000000005016 (35 java.util.Date)
	 * 
	 *  And from then on, no date instances are ever being collected.
	 *  Something must "hang" in the channel's per type entity chain or so
	 */
	static void doitSimple()
	{
		final Object[] data = generateGraphSimple(4);
		ROOT.set(data);

		final StorageConnection connection = STORAGE.createConnection();
		connection.storeRequired(ROOT);

		for(int i = 0; i < RUNS; i++)
		{
			final Date now = new Date();

			DEBUGStorage.println("#" + i + " storing @" + now.getTime());

			final Storer storer = connection.createStorer();

			for(int i3 = 0; i3 < data.length; i3++)
			{
				((TestEntity)data[i3]).setDate(now);
				storer.storeRequired(data[i3]);
			}

			final long dateOid = connection.persistenceManager().lookupObjectId(now);

			DEBUGStorage.println("#" + i + " storing " + storer.size());

			storer.commit();

			DEBUGStorage.println("#" + i + " stored." + " (" + dateOid + ")");

			JadothThreads.sleep(WAIT_TIME);
		}
	}


	static Object[][][] generateGraph(final int amount)
	{
//		final int length1 = 2;
//		final int length2 = 2;
//		final int length3 = Math.max(amount / 4, 1);
	
		final int length1 = 2;
		final int length2 = 1;
		final int length3 = 1;

		final Object[][][] e1 = new Object[length1][][];

		for(int i = 0; i < length1; i++)
		{
			final Object[][] e2 = e1[i] = new Object[length2][];

			for(int i2 = 0; i2 < length2; i2++)
			{
				final Object[] e3 = e2[i2] = new Object[length3];

				for(int i3 = 0; i3 < length3; i3++)
				{
					e3[i3] = new TestEntity(i3 + i2 * 10000 + i * 100000);
				}
			}
		}

		return e1;
	}
	
	static Object[] generateGraphSimple(final int amount)
	{
		final Object[] e3 = new Object[amount];

		for(int i3 = 0; i3 < amount; i3++)
		{
			e3[i3] = new TestEntity(i3 + i3 * 10000 + i3 * 100000);
		}

		return e3;
	}


	static class TestEntity
	{
		public int        id  ;
		public Lazy<Date> date;

		public TestEntity(final int id)
		{
			super();
			this.id = id;
		}

		public void setDate(final Date d)
		{
			this.date = Lazy.Reference(d);
		}



	}


	static void deleteOutput()
	{
		final File dir = new File("c:/Files");
		System.out.println("Resetting "+dir);
		UtilResetDirectory.deleteAllFiles(dir, false);
		System.out.println("done");
	}


}
