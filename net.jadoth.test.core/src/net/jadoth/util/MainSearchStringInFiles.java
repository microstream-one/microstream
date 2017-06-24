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
		// 1000000000049275254 ist das erste KonzAbschlagAnspruchAhVlUSt von KonzAbschlagAnsprueche hashTable 1000000000049005596
		searchStringsInFiles(DIRECT,
			new File("D:/BonusExportTest_2017-06-09_15-03-44.925/csv").listFiles(),
			"1000000000024649203"
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
