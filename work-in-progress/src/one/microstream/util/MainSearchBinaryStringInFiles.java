package one.microstream.util;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import one.microstream.X;
import one.microstream.chars.VarString;
import one.microstream.collections.XArrays;
import one.microstream.io.XPaths;
import one.microstream.memory.XMemory;

public class MainSearchBinaryStringInFiles
{
	static final BiConsumer<Path, Consumer<? super Path>> LOGGING = (f, p) ->
	{
		System.out.println("Processing "+f);
		p.accept(f);
		System.out.println(" * done processing "+f);
	};

	static final BiConsumer<Path, Consumer<? super Path>> DIRECT = (f, p) -> p.accept(f);

	
	private static void searchLCM(final long... objectIds)
	{
		for(final long objectId : objectIds)
		{
			searchLCM(objectId);
		}
	}
	
	private static void searchLCM(final long objectId)
	{
		System.out.println("\nSearching for " + objectId);
		searchStringsInFiles(
			DIRECT,
			XPaths.listChildrenUnchecked(XPaths.Path("D:/_Corp/20190313_2330_Rollback/garbage/")),
			XMemory.asByteArray(objectId)
		);
		searchStringsInFiles(
			DIRECT,
			X.array(XPaths.Path("D:/_Corp/20190313_2330_Rollback/cleaned/channel_0_864.dat")),
			XMemory.asByteArray(objectId)
		);
		searchStringsInFiles(
			DIRECT,
			X.array(XPaths.Path("D:/_Corp/2019-03-14_ProdDb/20190313_2330_autobackup_prod_kaputt/backup_daily_2019-03-13Z/channel_0/channel_0_872.dat")),
			XMemory.asByteArray(objectId)
		);
	}
	
	public static void main(final String[] args)
	{
		searchLCM(
			1000000000000098172L,
			1000000000000098219L
		);
		
//		searchStringsInFiles(
//			DIRECT,
//			X.array(new File("D:/Bonus25/storage/graveyard3/channel_3_663.dat")),
//			Memory.toByteArray(1000000000039080311L)
//		);

//		searchStringsInFiles(
//			DIRECT,
//			new File("D:/Bonus25/storage/graveyard2").listFiles(),
//			Memory.toByteArray(1000000000038806066L)
//		);

//		searchStringsInFiles(
//			DIRECT,
//			new File("D:/Bonus25/storage/graveyard3").listFiles(),
//			Memory.toByteArray(1000000000037420619L)
//		);

//		searchStringsInFiles(
//			DIRECT,
//			new File("C:/Bonus25/storage/channel_1").listFiles(),
//			XMemory.toByteArray(1000000000034381713L)
//		);
	}


	static void searchStringsInFiles(final Path[] files, final byte[]... strings)
	{
		searchStringsInFiles(DIRECT, files, strings);
	}

	static void searchStringsInFiles(
		final BiConsumer<Path, Consumer<? super Path>> logic  ,
		final Path[]                                   files  ,
		final byte[]...                                strings
	)
	{
		final long tStart = System.nanoTime();
		innerSearchStringsInFiles(logic, files, strings);
		System.gc();
		final long tStop = System.nanoTime();
		System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
	}

	static void innerSearchStringsInFiles(
		final BiConsumer<Path, Consumer<? super Path>> logic  ,
		final Path[]                                   files  ,
		final byte[]...                                strings
	)
	{
		for(final Path f : files)
		{
			if(XPaths.isDirectoryUnchecked(f))
			{
				innerSearchStringsInFiles(logic, XPaths.listChildrenUnchecked(f), strings);
			}
			else
			{
				logic.accept(f, file -> searchStringsInFile(file, strings));
			}

		}
	}


	static void searchStringsInFile(final Path f, final byte[]... strings)
		throws RuntimeException
	{
		try
		{
			final byte[] fileContent = XPaths.read_bytes(f);
			for(final byte[] s : strings)
			{
				int index = 0;
				while((index = XArrays.indexOf(fileContent, s, index)) >= 0)
				{
					System.out.println(index+"@"+f+" is "+VarString.New().addHexDec(s));
					index += s.length;
				}
			}
		}
		catch(final IOException e)
		{
			throw new RuntimeException(e);
		}

	}

}
