package com.mysql.dev.sakila;

import java.io.File;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;

import net.jadoth.math.XMath;
import net.jadoth.meta.XDebug;
import net.jadoth.reference.Reference;
import net.jadoth.storage.types.EmbeddedStorage;
import net.jadoth.storage.types.EmbeddedStorageManager;
import net.jadoth.storage.types.Storage;
import net.jadoth.test.corp.logic.Test;
import net.jadoth.test.corp.logic.TestImportExport;
import net.jadoth.time.XTime;

public class MainTestStoreCustomers
{
	/**
	 * Since the Jetstream storing performance considerably depends on the disk speed, it is important to use
	 * an SSD to get significant results.
	 */
	static final File DIRECTORY = new File("C:/" + MainTestStoreCustomers.class.getSimpleName());
	
	static
	{
		XDebug.deleteAllFiles(DIRECTORY, true);
	}
	
	/**
	 * Using multiple channels (a combination of a thread with an exclusive storage directory) is significant for
	 * any question concerning performance.
	 */
	static final int CHANNEL_COUNT = 4;

	static final Reference<ArrayList<Customer>> ROOT = Reference.New(new ArrayList<>());

	// create a storage manager, link the root, start the "embedded" database
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage
		.Foundation(
			DIRECTORY                                        , // location for the database files
			Storage.ChannelCountProvider(CHANNEL_COUNT)      , // amount of storage channels (parallel database threads)
			Storage.HousekeepingController(1000, 10_000_000) , // housekeeping time config (file cleanup, cache checks, etc.)
			Storage.DataFileEvaluator()                      , // evalutator for dissolving old files
			Storage.EntityCacheEvaluatorCustomTimeout(10_000)  // evalutator for unloading entities from the cache
		)
		.setRoot(ROOT)
		.start()
	;

	private static final int ENTITY_COUNT = 125_000;
	
	/**
	 * 1x Customer, 3x String, 3xLocalDate/Time, 1x Date.
	 */
	private static final int INSTANCE_COUNT = ENTITY_COUNT * 8;
	
	private static final int WARM_UP_RUNS = 3;
	private static final int RUNS         = 10;
	private static final int TOTAL_RUNS   = RUNS + WARM_UP_RUNS;
	
	
	public static void main(final String[] args)
	{
		final DecimalFormat countFormat = new java.text.DecimalFormat("000");
		final DecimalFormat timeFormat = new java.text.DecimalFormat("00,000,000,000");
		
		// assumed application logic that generate entities in some way. The generation is NOT part of the storing.
		final ArrayList<Customer> customers = generateEntities();

		long totalTime = 0;
		for(int r = 1; r <= TOTAL_RUNS; r++)
		{
			// resetting the state for the next test run is not part of the storing, either.
			resetTest();
			
			long tStart, tStop;
			tStart = System.nanoTime();
			STORAGE.store(customers);
			tStop = System.nanoTime();
						
			// printing and statistics
			if(r > WARM_UP_RUNS)
			{
				totalTime += tStop - tStart;
				final int run = r - WARM_UP_RUNS;
				final long averagePerRun = totalTime / run;
				final long averagePerEntity = averagePerRun / ENTITY_COUNT;
				System.out.println(
					"#" + countFormat.format(run) + " Elapsed Time (ns): "
					+ timeFormat.format(tStop - tStart)
					+ ", " + (tStop - tStart) / ENTITY_COUNT + " per entity. Average: "
					+ timeFormat.format(averagePerRun)
					+ ", " + averagePerEntity + " per entity."
				);
			}
			else
			{
				if(r == 0)
				{
					Test.print("Exporting data ..." );
					TestImportExport.testExport(STORAGE, Test.provideTimestampedDirectory(DIRECTORY, "export"));
					Test.print("Data export completed.");
				}
				
				System.out.println("Warmup... (" + (WARM_UP_RUNS - r + 1) + ")");
			}
		}
		
		System.out.println(
			"\nResult:\n"
			+ "Average per run     : " + timeFormat.format(totalTime / RUNS) + " ns.\n"
			+ "Average per entity  : " + timeFormat.format(totalTime / RUNS / ENTITY_COUNT) + " ns.\n"
			+ "Average per instance: " + timeFormat.format(totalTime / RUNS / INSTANCE_COUNT) + " ns.\n"
		);
		System.exit(0);
	}
	
	static ArrayList<Customer> generateEntities()
	{
		final Date baseDate       = XTime.date(XTime.currentYear(), 1, 1);
		final int  secondsPerYear = 60*60 * (24*365 + 24/4); // 365,25
		final ZoneId systemZoneId = ZoneId.systemDefault();
		
		final ArrayList<Customer> customers = ROOT.get();
		
		System.out.println("Generating Customer entities " + ENTITY_COUNT +" (" + INSTANCE_COUNT + " instances) ...");
		for(int i = 0; i < ENTITY_COUNT; i++)
		{
			final long creationTimestamp = baseDate.getTime() + XMath.random(secondsPerYear) * 1000;
			final long updateTimestamp   = creationTimestamp + XMath.random(Integer.MAX_VALUE); // ~25 days
			customers.add(
				new Customer(
					(short)i,
					(byte)XMath.random(Byte.MAX_VALUE),
					"FirstName" + i,
					"LastName" + i,
					"E-Mail@" + i,
					(short)XMath.random(Short.MAX_VALUE),
					XMath.random(10) >= 5,
					LocalDateTime.ofInstant(Instant.ofEpochMilli(updateTimestamp), systemZoneId),
					new Date(creationTimestamp)
				)
			);
		}

		System.out.println("* Generation complete." );
		
		return customers;
	}
	
	static void resetTest()
	{
		// the central object registry holding the internal object<->objectId associations must be reset
		STORAGE.persistenceManager().objectRegistry().truncate();
		System.gc();
	}

}
