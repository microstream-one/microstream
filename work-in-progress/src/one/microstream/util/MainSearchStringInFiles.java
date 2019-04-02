package one.microstream.util;

import java.io.File;
import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import one.microstream.chars.VarString;
import one.microstream.chars.XChars;
import one.microstream.files.XFiles;
import one.microstream.functional.TriConsumer;

public class MainSearchStringInFiles
{
	static final BiConsumer<File, Consumer<? super File>> LOGGING = (f, p) ->
	{
		System.out.println("Processing "+f);
		p.accept(f);
		System.out.println(" * done processing "+f);
	};

	static final BiConsumer<File, Consumer<? super File>> DIRECT = (f, p) -> p.accept(f);

	public static void main(final String[] args) throws Exception
	{
		searchStringsInFiles(DIRECT,
			new File("C:/BonusExportTest_2017-06-28_16-13-47.581/csv").listFiles(),
			MainSearchStringInFiles::printOid,
			
//			"1000000000056176168" // KeyAhVlUSt$Implementation
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


	static String[] loadIds(final File file, final String separator) throws Exception
	{
		final String fileContent = XFiles.readStringFromFileDefaultCharset(file);

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


	static void searchStringsInFiles(final File[] files, final String... strings)
	{
		searchStringsInFiles(files, null, strings);
	}
	
	static void searchStringsInFiles(
		final File[]                                files        ,
		final TriConsumer<String, Integer, Integer> matchCallback,
		final String...                             strings
	)
	{
		searchStringsInFiles(DIRECT, files, matchCallback, strings);
	}

	static void searchStringsInFiles(
		final BiConsumer<File, Consumer<? super File>> logic        ,
		final File[]                                    files        ,
		final TriConsumer<String, Integer, Integer>     matchCallback,
		final String...                                 strings
	)
	{
		final long tStart = System.nanoTime();
		for(final File f : files)
		{
			logic.accept(f, file -> searchStringsInFile(file, matchCallback, strings));
			System.gc();
		}
		final long tStop = System.nanoTime();
		System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));
	}

	static void searchStringsInFile(
		final File                                  f            ,
		final TriConsumer<String, Integer, Integer> matchCallback,
		final String...                             strings
	)
		throws RuntimeException
	{
		try
		{
			final String fileContent = XFiles.readStringFromFileDefaultCharset(f);
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
