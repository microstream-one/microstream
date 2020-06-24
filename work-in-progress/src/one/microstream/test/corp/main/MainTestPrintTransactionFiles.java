package one.microstream.test.corp.main;

import one.microstream.afs.nio.NioFileSystem;
import one.microstream.io.XIO;
import one.microstream.test.corp.logic.Test;

public class MainTestPrintTransactionFiles
{
	public static void main(final String[] args)
	{
		Test.printTransactionsFiles(NioFileSystem.Directory(XIO.Path("d:/StorageTest")), 4);
	}
}
