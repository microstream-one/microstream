package one.microstream.test.afs;

import one.microstream.chars.XChars;
import one.microstream.collections.XArrays;

public class AfsTest
{
	static final String[] TEST_DIRECTORY_PATH = {"D:", "testDir", "testSubDir"};
	static final String[] TEST_FILE_PATH      = XArrays.add(TEST_DIRECTORY_PATH, "file.txt");
	
	
	static <T> void mustBeSame(final T t1, final T t2)
	{
		if(t1 == t2)
		{
			return;
		}
		
		// (02.06.2020 TM)EXCP: proper exception
		throw new RuntimeException(
			"Not the same instance: " + XChars.systemString(t1) + " != " + XChars.systemString(t2)
		);
	}
}
