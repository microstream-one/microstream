package net.jadoth.csv.internal;

import java.io.File;
import java.io.IOException;

import net.jadoth.chars.StringTable;
import net.jadoth.chars._charArrayRange;
import net.jadoth.file.JadothFiles;
import net.jadoth.typing.KeyValue;
import net.jadoth.util.csv.CsvContent;
import net.jadoth.util.csv.CsvContentBuilderCharArray;

public class MainTestCsvFunctionality
{
//	static final CsvConfiguration CONFIG = CSV.configurationBuilder()
////		.setValueSeparator('\t')
////		.setValueSeparator(',')
////		.setLineSeparator("|")
////		.setSkipLineCount(3)
////		.setSkipLineCountPostHeader(2)
////		.setTerminator(')')
////		.setSkipLineCountTrailing(3)
//		.createConfiguration()
//	;

	static final CsvContentBuilderCharArray BUILDER = CsvContentBuilderCharArray.New();

	static final File     DIR   = new File("D:/xcsv/");
	static final String[] FILES = {
		"xcsv_sample_01.xcsv",
		"xcsv_sample_02.xcsv",
		"xcsv_sample_03.xcsv",
		"xcsv_sample_04.xcsv",
		"xcsv_sample_05.xcsv",
		"xcsv_sample_06.xcsv",
		"xcsv_sample_07.xcsv",
		"xcsv_sample_08.xcsv",
//		"xcsv_sample_09.xcsv"
//		"xcsv_sample_10.xcsv"
//		"xcsv_bigsample.xcsv"
	};



	public static void main(final String[] args) throws Throwable
	{
		for(final String file : FILES)
		{
			parseXCsv(new File(DIR, file));
		}
	}

	static final void parseXCsv(final File xcsv) throws IOException
	{
		System.out.println("||||||||||||||||||||");
		System.out.println(xcsv);
		System.out.println("||||||||||||||||||||");
		final char[] input = JadothFiles.readCharsFromFile(xcsv);

		for(int i = 1; i --> 0;)
		{
			final long tStart = System.nanoTime();
			final CsvContent tables = BUILDER.build(xcsv.getName(), _charArrayRange.New(input));
			final long tStop = System.nanoTime();
			System.out.println("Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));

			for(final KeyValue<String, StringTable> e : tables.segments())
			{
				System.out.println(e.key()+":");
				if(e.value().rows().size() > 100)
				{
					System.out.println("["+e.value().rows().size()+" rows]");
				}
				else
				{
					System.out.println(StringTable.Static.assembleString(e.value()));
				}
				System.out.println("-----------");
			}
		}

		System.out.println();
	}

}
