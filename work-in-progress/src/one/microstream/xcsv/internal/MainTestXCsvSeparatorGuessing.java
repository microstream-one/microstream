package one.microstream.xcsv.internal;

import java.nio.file.Path;

import one.microstream.chars.StringTable;
import one.microstream.io.XIO;
import one.microstream.util.xcsv.XCSV;

public class MainTestXCsvSeparatorGuessing
{
	static final Path DIR = XIO.Path("D:/xcsv/");
	
	static final String[] FILES = {
		"xcsv_separatorGuessing_01.xcsv",
		"xcsv_separatorGuessing_02.xcsv",
		"xcsv_separatorGuessing_03.xcsv",
		"xcsv_separatorGuessing_04.csv"
	};

	public static void main(final String[] args) throws Throwable
	{
		for(final String file : FILES)
		{
			System.out.println(file);
			
			// see XCSV#parse methods for direct string parsing instead of reading from a file first.
			final StringTable stringTable = XCSV.readFromFile(XIO.Path(DIR, file));
			System.out.println(XCSV.assembleString(stringTable));
			System.out.println();
		}
	}

}

/* --------- test data --------- *\

xcsv_separatorGuessing_01.xcsv
col1,col2,col3
1.5,1.1,0.9
1.5,1.1,0.9
1.5,1.1,0.9
1.5,1.1,0.9

xcsv_separatorGuessing_02.xcsv
col1,col2,col3,col4
1.5,1.1,0.9,"blabla;bla;blabla;"
1.5,1.1,0.9,"blabla;bla"
1.5,1.1,0.9,"blabla"
1.5,1.1,0.9,"blabla;bla"

xcsv_separatorGuessing_03.xcsv
col1
"tricky"
"tricky"
"tricky"

xcsv_separatorGuessing_04.csv
col1; col2; col3
1,1; 1,2; 1,3
2,1; 2,2; 2,3

\*-------------------------------*/