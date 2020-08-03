package com.mysql.dev.sakila;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;

import one.microstream.afs.ADirectory;
import one.microstream.afs.nio.NioFileSystem;
import one.microstream.io.XIO;
import one.microstream.math.XMath;
import one.microstream.meta.XDebug;
import one.microstream.reference.Reference;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.storage.types.Storage;
import one.microstream.test.corp.logic.Test;
import one.microstream.test.corp.logic.TestImportExport;
import one.microstream.time.XTime;


public class MainTestStoreSakilaCustomersPerformance
{
	/**
	 * Since the MicroStream storing performance considerably depends on the disk speed, it is important to use
	 * an SSD to get significant results.
	 */
	static final ADirectory DIRECTORY = NioFileSystem.New().ensureDirectory(
		XIO.Path("C:/" + MainTestStoreSakilaCustomersPerformance.class.getSimpleName())
	);
	
	static
	{
		// the database directory is completely cleaned before every execution.
		XDebug.deleteAllFiles(DIRECTORY, true);
	}
	
	/**
	 * Using multiple channels (a combination of a thread with an exclusive storage directory) is significant for
	 * any question concerning performance.
	 */
	static final int CHANNEL_COUNT = 1;

	/**
	 * The application's entity graph root node/instance.
	 */
	static final Reference<ArrayList<Customer>> ROOT = Reference.New(new ArrayList<>());

	/**
	 * The control instance managing the database with arguments specific for this test.
	 */
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage
		.Foundation(
			Storage.ConfigurationBuilder()
			.setStorageFileProvider   (Storage.FileProvider(DIRECTORY)            ) // location for the database files
			.setChannelCountProvider  (Storage.ChannelCountProvider(CHANNEL_COUNT)) // amount of storage channels (parallel database threads)
			.setHousekeepingController(Storage.HousekeepingController()           ) // housekeeping time config (file cleanup, cache checks, etc.)
			.setDataFileEvaluator         (Storage.DataFileEvaluator()                ) // evaluator for dissolving data files to optimize disc usage.
			.setEntityCacheEvaluator  (Storage.EntityCacheEvaluator()             ) // evaluator for unloading entity data from the storage cache.
		)
		.setRoot(ROOT)
		.start()
	;

	/**
	 * The amount of entities to be created.
	 */
	private static final int ENTITY_COUNT = 125_000;
	
	/**
	 * 1x Customer, 3x String, 3xLocalDate/Time, 1x Date.
	 */
	private static final int INSTANCE_COUNT = ENTITY_COUNT * 8;
	
	/**
	 * See (google) the complex topic "Java performance measuring".
	 * Warm-up runs are needed to account for class loading, JVM code optimizing etc.
	 * Measured times can vary from run to run for a variety of reasons (code optimization work, OS interrupts, etc.)
	 */
	private static final int WARM_UP_RUNS = 3;
	private static final int RUNS         = 10;
	private static final int TOTAL_RUNS   = RUNS + WARM_UP_RUNS;
	
	
	
	public static void main(final String[] args)
	{
		final DecimalFormat countFormat = new java.text.DecimalFormat("000");
		final DecimalFormat timeFormat = new java.text.DecimalFormat("00,000,000,000");
		
		// assumed application logic that generate entities in some way. The generation is NOT part of the storing.
		final ArrayList<Customer> customers = generateEntities();

		System.out.println("Storing entities ...");
		
		long totalTime = 0;
		for(int r = 1; r <= TOTAL_RUNS; r++)
		{
			// resetting the state for the next test run is not part of the storing, either.
			resetTest();
			
			long tStart, tStop;
			
			// the actual work (storing all entities) that is being measured.
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
		
		// result
		System.out.println(
			"\nResult:\n"
			+ "Average per run     : " + timeFormat.format(totalTime / RUNS) + " ns.\n"
			+ "Average per entity  : " + timeFormat.format(totalTime / RUNS / ENTITY_COUNT) + " ns.\n"
			+ "Average per instance: " + timeFormat.format(totalTime / RUNS / INSTANCE_COUNT) + " ns.\n"
		);
		
		// exit (no shutdown needed, the database is guaranteed to always be in a consistent state)
		System.exit(0);
	}
	
	/**
	 * Generates {@value #ENTITY_COUNT} entities with generic / random data.
	 * 
	 * @return the ROOT's ArrayList instance filled with the generated entities.
	 */
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
	
	/**
	 * Work that must be done to reset the application's state for each run to get reasonable results.
	 */
	static void resetTest()
	{
		// the central object registry holding the internal object<->objectId associations must be reset
		STORAGE.persistenceManager().objectRegistry().clear();
		System.gc();
	}

}
