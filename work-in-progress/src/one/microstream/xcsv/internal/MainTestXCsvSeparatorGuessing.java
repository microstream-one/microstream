package one.microstream.xcsv.internal;

import java.nio.file.Path;

import one.microstream.chars.StringTable;
import one.microstream.io.XIO;
import one.microstream.util.xcsv.XCSV;

public class MainTestXCsvSeparatorGuessing
{
	static final Path DIR = XIO.Path("D:/xcsv/");
	
	static final String[] FILES = {
//		"xcsv_separatorGuessing_01.xcsv",
//		"xcsv_separatorGuessing_02.xcsv",
//		"xcsv_separatorGuessing_03.xcsv",
		"xcsv_separatorGuessing_04.csv"
	};

	public static void main(final String[] args) throws Throwable
	{
		for(final String file : FILES)
		{
			System.out.println(file);
			final StringTable stringTable = XCSV.readFromFile(XIO.Path(DIR, file));
			System.out.println(StringTable.Static.assembleString(stringTable));
		}
	}
	

}

/* --------- test data --------- *\
// (13.02.2020 TM)FIXME: priv#204: add test data

xcsv_separatorGuessing_04.csv
col1; col2; col3
1,1; 1,2; 1,3
2,1; 2,2; 2,3


\*-------------------------------*/