package one.microstream.test.corp.main;

import one.microstream.io.XPaths;
import one.microstream.test.corp.logic.Test;

public class MainTestPrintTransactionFiles
{
	public static void main(final String[] args)
	{
		Test.printTransactionsFiles(XPaths.Path("d:/StorageTest"), 4);
	}
}
