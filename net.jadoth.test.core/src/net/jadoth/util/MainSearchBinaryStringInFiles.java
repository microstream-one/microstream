package net.jadoth.util;

import java.io.File;
import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import net.jadoth.chars.VarString;
import net.jadoth.collections.XArrays;
import net.jadoth.files.XFiles;
import net.jadoth.memory.Memory;

public class MainSearchBinaryStringInFiles
{
	static final BiConsumer<File, Consumer<? super File>> LOGGING = (f, p) ->
	{
		System.out.println("Processing "+f);
		p.accept(f);
		System.out.println(" * done processing "+f);
	};

	static final BiConsumer<File, Consumer<? super File>> DIRECT = (f, p) -> p.accept(f);

	public static void main(final String[] args)
	{
//		searchStringsInFiles(
//			DIRECT,
//			Jadoth.array(new File("D:/Bonus25/storage/graveyard3/channel_3_663.dat")),
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

		searchStringsInFiles(
			DIRECT,
			new File("C:/Bonus25/storage/channel_1").listFiles(),
			Memory.toByteArray(1000000000034381713L)
		);
	}


	static void searchStringsInFiles(final File[] files, final byte[]... strings)
	{
		searchStringsInFiles(DIRECT, files, strings);
	}

	static void searchStringsInFiles(
		final BiConsumer<File, Consumer<? super File>> logic  ,
		final File[]                                    files  ,
		final byte[]...                                 strings
	)
	{
		final long tStart = System.nanoTime();
		innerSearchStringsInFiles(logic, files, strings);
		System.gc();
		final long tStop = System.nanoTime();
		System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
	}

	static void innerSearchStringsInFiles(
		final BiConsumer<File, Consumer<? super File>> logic  ,
		final File[]                                     files  ,
		final byte[]...                                  strings
	)
	{
		for(final File f : files)
		{
			if(f.isDirectory())
			{
				innerSearchStringsInFiles(logic, f.listFiles(), strings);
			}
			else
			{
				logic.accept(f, file -> searchStringsInFile(file, strings));
			}

		}
	}


	static void searchStringsInFile(final File f, final byte[]... strings)
		throws RuntimeException
	{
		try
		{
			final byte[] fileContent = XFiles.readBytesFromFile(f);
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
