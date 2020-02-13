package one.microstream.xcsv.internal;

import java.io.IOException;
import java.nio.file.Path;

import one.microstream.chars.StringTable;
import one.microstream.chars.XChars;
import one.microstream.chars._charArrayRange;
import one.microstream.io.XIO;
import one.microstream.typing.KeyValue;
import one.microstream.util.xcsv.XCSV;
import one.microstream.util.xcsv.XCsvContent;
import one.microstream.util.xcsv.XCsvContentBuilderCharArray;

public class MainTestXCsvFunctionality
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

	static final XCsvContentBuilderCharArray BUILDER = XCsvContentBuilderCharArray.New();

	static final Path     DIR   = XIO.Path("D:/xcsv/");
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
			parseXCsv(XIO.Path(DIR, file));
		}
	}

	static final void parseXCsv(final Path xcsv) throws IOException
	{
		System.out.println("||||||||||||||||||||");
		System.out.println(xcsv);
		System.out.println("||||||||||||||||||||");
		final char[] input = XIO.readString(xcsv, XChars.defaultJvmCharset()).toCharArray();

		for(int i = 1; i --> 0;)
		{
			final long tStart = System.nanoTime();
			final XCsvContent tables = BUILDER.build(XIO.getFileName(xcsv), _charArrayRange.New(input));
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
					System.out.println(XCSV.assembleString(e.value()));
				}
				System.out.println("-----------");
			}
		}

		System.out.println();
	}

}
