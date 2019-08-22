package one.microstream.persistence.test;

import java.io.File;
import java.util.Date;

import one.microstream.concurrency.XThreads;
import one.microstream.meta.XDebug;
import one.microstream.persistence.lazy.Lazy;
import one.microstream.persistence.types.Storer;
import one.microstream.storage.types.DebugStorage;
import one.microstream.storage.types.StorageConnection;


/*
 * Test to investigate why/how (really if) initial dummy instances that get replaced afterwards
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
 *
 */
public class MainTestStorageDummyInstanceGCTest extends TestStorage
{
	static final int  RUNS      = 10000;
	static final long WAIT_TIME = 4000;

	public static void main(final String[] args)
	{
//		deleteOutput();
		STORAGE.start();

//		doit();
//		doitSimple();
		doitSimple_NoPerson();
		exit();
	}

	static void doit()
	{
		final Object[][][] data = generateGraph(4);
		ROOT.set(data);

		final StorageConnection connection = STORAGE.createConnection();
		connection.store(ROOT);

		for(int i = 0; i < RUNS; i++)
		{
			final Date now = new Date();

			DebugStorage.println("#" + i + " storing @" + now.getTime());

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
						storer.store(e3[i3]);
					}
				}
			}

			final long dateOid = connection.persistenceManager().lookupObjectId(now);

			DebugStorage.println("#" + i + " storing " + storer.size());

			storer.commit();

			DebugStorage.println("#" + i + " stored." + " (" + dateOid + ")");

			XThreads.sleep(WAIT_TIME);
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
		final Object[] data = generateGraphSimple(2);
		ROOT.set(data);

		final StorageConnection connection = STORAGE.createConnection();
		connection.store(ROOT);

		for(int i = 0; i < RUNS; i++)
		{
			final Date now = new Date();

			DebugStorage.println("#" + i + " storing @" + now.getTime());

			final Storer storer = connection.createStorer();

			for(int i3 = 0; i3 < data.length; i3++)
			{
				((TestEntity)data[i3]).setDate(now);
				storer.store(data[i3]);
			}

			final long dateOid = connection.persistenceManager().lookupObjectId(now);

			DebugStorage.println("#" + i + " storing " + storer.size());

			storer.commit();

			DebugStorage.println("#" + i + " stored." + " (" + dateOid + ")");

			XThreads.sleep(WAIT_TIME);
		}
	}

	static void doitSimple_NoPerson()
	{
		final Object[] data = generateGraphSimple(2, new Date());
		ROOT.set(data);

		final StorageConnection connection = STORAGE.createConnection();
		connection.store(ROOT);

		for(int i = 0; i < RUNS; i++)
		{
			final Date now = new Date();

			DebugStorage.println("#" + i + " storing @" + now.getTime());

			final Storer storer = connection.createStorer();

			for(int i3 = 0; i3 < data.length; i3++)
			{
				data[i3] = Lazy.Reference(now);
			}
			storer.store(data);

			final long dateOid = connection.persistenceManager().lookupObjectId(now);

			DebugStorage.println("#" + i + " storing " + storer.size());

			storer.commit();

			DebugStorage.println("#" + i + " stored." + " (" + dateOid + ")");

			XThreads.sleep(WAIT_TIME);
		}
	}

	static Object[] generateGraphSimple(final int amount, final Date d)
	{
		final Object[] e3 = new Object[amount];

		for(int i3 = 0; i3 < amount; i3++)
		{
			e3[i3] = Lazy.Reference(d);
		}

		return e3;
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
		XDebug.deleteAllFiles(dir, false);
		System.out.println("done");
	}


}
