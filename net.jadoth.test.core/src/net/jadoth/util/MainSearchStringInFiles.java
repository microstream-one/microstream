package net.jadoth.util;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import net.jadoth.functional.BiProcedure;
import net.jadoth.functional.TriConsumer;
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
		searchStringsInFiles(DIRECT,
			new File("C:/BonusExportTest_2017-06-27_15-36-07.196/csv").listFiles(),
			MainSearchStringInFiles::printOid,
//			"1000000000025679576" // 0 ZahlungAusschuettungbetrag$Implementation
//			"1000000000025680038" // 1 = 0 referenzierende ZahlungAuszahlung$Implementation
//			"1000000000025679571" // 2 = 0 referenzierende HashTable
			"1000000000025679567" // 4 = 2 referenzierende Lazy
			
			// KonzAusAnspruchAhg
//			"1000000000039543476" // 0 KonzAusAnspruchAhg$Implementation
//			"1000000000039546071" // 1 = 0 referenzierende ZahlungAusschuettungbetrag$Implementation
//			"1000000000039546799" // 2 = 1 referenzierende ZahlungAuszahlung$Implementation
//			"1000000000039546067" // 3 = 1 referenzierende HashTable
//			"1000000000039546063" // 5 = 3 referenzierende Lazy
//			"1000000000039530216" // 7 = 5 referenzierende VorgangKonzAusschuettungNachStaffel
			
			// HausAusAnspruchAhg
//			"1000000000025153392" // 0 HausAusAnspruchAhg$Implementation
//			"1000000000025155744" // 1 = 0 referenzierende ZahlungAusschuettungbetrag$Implementation
//			"1000000000025155738" // 2 = 1 referenzierende HashTable
//			"1000000000025155897" // 3 = 1 referenzierende ZahlungAuszahlung$Implementation
//			"1000000000025155734" // 4 = 2 referenzierende Lazy (#ausschuettungsbetraege)
//			"1000000000025155740" // 5 = 3 referenzierende HashTable
//			"1000000000025150620" // 6 = 4 referenzierende VorgangHausAusschuettung
//			"1000000000025155736" // 7 = 5 referenzierende Lazy (#auszahlungen)
//			"1000000000025150620" // 6 = 7 referenzierende VorgangHausAusschuettung
		);
	}
	
	static void printOid(final String s, final Integer index, final Integer endIndex)
	{
		final int newLineIndex = s.lastIndexOf('\n', index);
		// 20 ist die Länge einer OID
		System.out.println("Line: " + s.substring(newLineIndex + 1, newLineIndex + 1 + 20));
//		System.out.println("Line: " + s.substring(newLineIndex + 1, endIndex));
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
		final BiProcedure<File, Consumer<? super File>> logic        ,
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
			final String fileContent = JadothFiles.readStringFromFile(f);
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
