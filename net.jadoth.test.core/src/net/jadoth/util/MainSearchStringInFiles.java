package net.jadoth.util;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import net.jadoth.functional.BiProcedure;
import net.jadoth.util.chars.VarString;
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

	public static void main(final String[] args) throws Exception
	{
		// Fall 1
//		searchStringsInFiles(LOGGING, new File("D:/BonusExportTest/csv").listFiles(), "1000000000024612480");
//		searchStringsInFiles(LOGGING, new File("D:/BonusExportTest/csv").listFiles(), "1000000000024612290");

		// Fall 2
//		searchStringsInFiles(DIRECT, new File("D:/BonusExportTest/csv").listFiles(), "26920708");
//		searchStringsInFiles(DIRECT, new File("D:/BonusExportTest_2015-10-16_10-13-11.301/csv").listFiles(), "1000000000000000008");

//		searchStringsInFiles(DIRECT, new File("D:/BonusExportTest_2016-07-20_12-53-40.706/csv").listFiles(), "1000000000044806574");


//		searchStringsInFiles(LOGGING,
//			new File("D:/BonusExportTest_2016-11-11_16-01-29.518/csv").listFiles(),
//			completeIds(loadIds(new File("P:/Integration/2016/Zombie References 2016-11-11/Zombie OIDs 2016-11-11 Search.txt"), "\\n"), "10000000000")
//		);

		// 1000000000046756227 ist Lazy mit Referenz auf Zombie 1000000000046756228
//		searchStringsInFiles(DIRECT,
//			new File("D:/BonusExportTest_2016-11-11_16-01-29.518/csv").listFiles(),
//			"1000000000046756227"
//		);
//		searchStringsInFiles(DIRECT,
//			new File("D:/BonusExportTest_2016-11-11_16-35-09.436/csv").listFiles(),
//			"1000000000046756227"
//		);

		// 1000000000049005596 ist EqHashTable mit Referenz auf zombie
//		searchStringsInFiles(DIRECT,
//			new File("D:/BonusExportTest_2016-11-11_16-01-29.518/csv").listFiles(),
//			"1000000000049005596"
//		);
//		searchStringsInFiles(DIRECT,
//			new File("D:/BonusExportTest_2016-11-11_16-35-09.436/csv").listFiles(),
//			"1000000000049005596"
//		);

		// konzabansprüche 1000000000049005586 referenziert 1000000000049005596
//		searchStringsInFiles(DIRECT,
//			new File("D:/BonusExportTest_2016-11-11_16-35-09.436/csv").listFiles(),
//			"1000000000049005586"
//		);

		// lazy 1000000000049005584 ref 1000000000049005586
//		searchStringsInFiles(DIRECT,
//			new File("D:/BonusExportTest_2016-11-11_16-35-09.436/csv").listFiles(),
//			"1000000000049005584"
//		);


		// 1000000000049275254 ist das erste KonzAbschlagAnspruchAhVlUSt von KonzAbschlagAnsprueche hashTable 1000000000049005596
		searchStringsInFiles(DIRECT,
			new File("D:/BonusExportTest_2017-03-06_13-56-13.733/csv").listFiles(),
			"1000000000020811816"
		);

	}


	static String[] loadIds(final File file, final String separator) throws Exception
	{
		final String fileContent = JadothFiles.readStringFromFile(file);

		final String[] parts = fileContent.split(separator);

		return parts;
	}

	static String[] completeIds(final String[] ids, final String prefix)
	{
		final char[] chars = prefix.toCharArray();

		final VarString vs = VarString.New();

		final String[] result = new String[ids.length];

		for(int i = 0; i < ids.length; i++)
		{
			result[i] = vs.reset().add(chars).add(ids[i]).toString();
		}

		return result;
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
