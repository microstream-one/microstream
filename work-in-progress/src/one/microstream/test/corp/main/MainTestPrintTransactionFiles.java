package one.microstream.test.corp.main;

import java.nio.file.Path;

import one.microstream.afs.ADirectory;
import one.microstream.afs.nio.NioFileSystem;
import one.microstream.io.XIO;
import one.microstream.test.corp.logic.Test;

public class MainTestPrintTransactionFiles
{
	public static void main(final String[] args)
	{
		final Path storageDirPath = XIO.Path("D:\\workspaces\\microstream\\work-in-progress\\storage");
		final ADirectory storageDirectory = NioFileSystem.New().ensureDirectory(storageDirPath);
		Test.printTransactionsFiles(storageDirectory, 1);
	}
}
