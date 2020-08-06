package one.microstream.util;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import one.microstream.chars.VarString;
import one.microstream.collections.XArrays;
import one.microstream.io.XIO;
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

	
	static void searchLongValues(final long... longValues)
	{
		for(final long objectId : longValues)
		{
			searchLongValue(objectId);
		}
	}
	
	private static void searchLongValue(final long longValue)
	{
		final byte[] byteString = XMemory.asByteArray(longValue);
		
		System.out.println("\nSearching for " + longValue);
		searchStringsInDirectory(
			XIO.Path("C:/my/app/path/storage/"),
			byteString
		);
	}
	
	public static void main(final String[] args)
	{
//		System.out.println(System.currentTimeMillis());
//		System.out.println(XTime.timestamp(2019, 11, 18).getTime());
		
//		Test.printTransactionsFiles(XIO.Path("C:/my/app/path/storage"), 1);
		
		searchLongValues(
			1000000000001703272L,
			1000000000000097599L
		);
	}

	static void searchStringsInDirectory(final Path directory, final byte[]... strings)
	{
		searchStringsInDirectory(DIRECT, directory, strings);
	}
	

	static void searchStringsInFiles(final Path[] files, final byte[]... strings)
	{
		searchStringsInFiles(DIRECT, files, strings);
	}
	
	static void searchStringsInDirectory(
		final BiConsumer<Path, Consumer<? super Path>> logic    ,
		final Path                                     directory,
		final byte[]...                                strings
	)
	{
		System.out.println("\nSearching in directory " + directory);
		searchStringsInFiles(logic, XIO.unchecked.listEntries(directory), strings);
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
			if(XIO.unchecked.isDirectory(f))
			{
				innerSearchStringsInFiles(logic, XIO.unchecked.listEntries(f), strings);
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
			final byte[] fileContent = XIO.read_bytes(f);
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
