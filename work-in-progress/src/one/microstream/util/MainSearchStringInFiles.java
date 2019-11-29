package one.microstream.util;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import one.microstream.chars.VarString;
import one.microstream.chars.XChars;
import one.microstream.functional.TriConsumer;
import one.microstream.io.XIO;

public class MainSearchStringInFiles
{
	static final BiConsumer<Path, Consumer<? super Path>> LOGGING = (f, p) ->
	{
		System.out.println("Processing "+f);
		p.accept(f);
		System.out.println(" * done processing "+f);
	};

	static final BiConsumer<Path, Consumer<? super Path>> DIRECT = (f, p) -> p.accept(f);

	public static void main(final String[] args) throws Exception
	{
		searchStringsInFiles(DIRECT,
			XIO.listEntries(XIO.Path("C:/BonusExportTest_2017-06-28_16-13-47.581/csv")),
			MainSearchStringInFiles::printOid,
			
//			"1000000000056176168" // KeyAhVlUSt$Default
			"1000000000054447655" // EqHashEnum
		);
	}
	
	static void printOid(final String s, final Integer index, final Integer endIndex)
	{
		final int newLineIndex = s.lastIndexOf('\n', index);
		// 20 is the length of an OID
		System.out.println("Line: " + s.substring(newLineIndex + 1, newLineIndex + 1 + 20));
//		System.out.println("Line: " + s.substring(newLineIndex + 1, endIndex));
	}


	static String[] loadIds(final Path file, final String separator) throws Exception
	{
		final String fileContent = XIO.readString(file, XChars.defaultJvmCharset());

		final String[] parts = fileContent.split(separator);

		return parts;
	}

	static String[] completeIds(final String[] ids, final String prefix)
	{
		final char[] chars = XChars.readChars(prefix);

		final VarString vs = VarString.New();

		final String[] result = new String[ids.length];

		for(int i = 0; i < ids.length; i++)
		{
			result[i] = vs.reset().add(chars).add(ids[i]).toString();
		}

		return result;
	}


	static void searchStringsInFiles(final Path[] files, final String... strings)
	{
		searchStringsInFiles(files, null, strings);
	}
	
	static void searchStringsInFiles(
		final Path[]                                files        ,
		final TriConsumer<String, Integer, Integer> matchCallback,
		final String...                             strings
	)
	{
		searchStringsInFiles(DIRECT, files, matchCallback, strings);
	}

	static void searchStringsInFiles(
		final BiConsumer<Path, Consumer<? super Path>> logic        ,
		final Path[]                                    files        ,
		final TriConsumer<String, Integer, Integer>     matchCallback,
		final String...                                 strings
	)
	{
		final long tStart = System.nanoTime();
		for(final Path f : files)
		{
			logic.accept(f, file -> searchStringsInFile(file, matchCallback, strings));
			System.gc();
		}
		final long tStop = System.nanoTime();
		System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
	}

	static void searchStringsInFile(
		final Path                                  f            ,
		final TriConsumer<String, Integer, Integer> matchCallback,
		final String...                             strings
	)
		throws RuntimeException
	{
		try
		{
			final String fileContent = XIO.readString(f, XChars.defaultJvmCharset());
			for(final String s : strings)
			{
				final int index = fileContent.indexOf(s);
				if(index >= 0)
				{
					System.out.println(index + "@" + f + " is " + s);
					if(matchCallback != null)
					{
						matchCallback.accept(fileContent, index, index + s.length());
					}
				}
			}
		}
		catch(final IOException e)
		{
			throw new RuntimeException(e);
		}
	}

}
