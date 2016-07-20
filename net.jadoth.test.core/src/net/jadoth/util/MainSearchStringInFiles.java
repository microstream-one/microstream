package net.jadoth.util;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import net.jadoth.functional.BiProcedure;
import net.jadoth.util.file.JadothFiles;

public class MainSearchStringInFiles
{
	static final BiProcedure<File, Consumer<? super File>> LOGGING = (f, p) ->
	{
		System.out.println("Processing "+f);
		p.accept(f);
		System.out.println(" * done processing "+f);
	};

	static final BiProcedure<File, Consumer<? super File>> DIRECT = (f, p) -> p.accept(f);

	public static void main(final String[] args)
	{
		// Fall 1
//		searchStringsInFiles(LOGGING, new File("D:/BonusExportTest/csv").listFiles(), "1000000000024612480");
//		searchStringsInFiles(LOGGING, new File("D:/BonusExportTest/csv").listFiles(), "1000000000024612290");

		// Fall 2
//		searchStringsInFiles(DIRECT, new File("D:/BonusExportTest/csv").listFiles(), "26920708");
//		searchStringsInFiles(DIRECT, new File("D:/BonusExportTest_2015-10-16_10-13-11.301/csv").listFiles(), "1000000000000000008");

		searchStringsInFiles(DIRECT, new File("D:/BonusExportTest_2016-07-20_12-53-40.706/csv").listFiles(), "1000000000044806574");


		// 1000000000040456642 ist eqHashTable mit UmsatzAhVlUst 1000000000040485361

	}


	static void searchStringsInFiles(final File[] files, final String... strings)
	{
		searchStringsInFiles(DIRECT, files, strings);
	}

	static void searchStringsInFiles(
		final BiProcedure<File, Consumer<? super File>> logic  ,
		final File[]                                     files  ,
		final String...                                  strings
	)
	{
		final long tStart = System.nanoTime();
		for(final File f : files)
		{
			logic.accept(f, file -> searchStringsInFile(file, strings));
			System.gc();
		}
		final long tStop = System.nanoTime();
		System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
	}

	static void searchStringsInFile(final File f, final String... strings)
		throws RuntimeException
	{


		try
		{
			final String fileContent = JadothFiles.readStringFromFile(f);
			for(final String s : strings)
			{
				final int index = fileContent.indexOf(s);
				if(index >= 0)
				{
					System.out.println(index+"@"+f+" is "+s);
				}
			}
		}
		catch(final IOException e)
		{
			throw new RuntimeException(e);
		}

	}

}
