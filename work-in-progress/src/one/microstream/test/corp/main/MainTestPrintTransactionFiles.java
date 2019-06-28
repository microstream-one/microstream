package one.microstream.test.corp.main;

import java.io.File;

import one.microstream.test.corp.logic.Test;

public class MainTestPrintTransactionFiles
{
	public static void main(final String[] args)
	{
		Test.printTransactionsFiles(new File("d:/StorageTest"), 4);
	}
}
