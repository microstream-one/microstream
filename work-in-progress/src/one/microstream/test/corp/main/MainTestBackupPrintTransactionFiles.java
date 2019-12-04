package one.microstream.test.corp.main;

import one.microstream.chars.VarString;
import one.microstream.io.XIO;
import one.microstream.storage.types.StorageFileProvider;
import one.microstream.storage.types.StorageTransactionsFileAnalysis;

public class MainTestBackupPrintTransactionFiles
{
	// local copy to prevent storage starting caused by referencing a constant in MainTestBackupStoring (crazy! :D)
	static final String DIRECTORY_STORAGE = StorageFileProvider.Defaults.defaultStorageDirectory();
	static final String DIRECTORY_BACKUP  = DIRECTORY_STORAGE + "/backup";
	
	
	public static void main(final String[] args)
	{
		printTransactionsFiles();
	}
	
	public static void printTransactionsFiles()
	{
		printTransactionsFiles(DIRECTORY_STORAGE, 1);
		printTransactionsFiles(DIRECTORY_BACKUP, 1);
	}
	
	public static void printTransactionsFiles(final String baseDirectory, final int channelCount)
	{
		System.out.println(baseDirectory);
		System.out.println(StorageTransactionsFileAnalysis.EntryAssembler.assembleHeader(VarString.New(), "\t"));
		for(int i = 0; i < channelCount; i++)
		{
			final VarString vs = StorageTransactionsFileAnalysis.Logic.parseFile(
				XIO.Path(baseDirectory
					+ "/" + StorageFileProvider.Defaults.defaultChannelDirectoryPrefix() + i
					+"/" + StorageFileProvider.Defaults.defaultTransactionFilePrefix() + i
					+ StorageFileProvider.Defaults.defaultTransactionFileSuffix()
				)
			);
			System.out.println(vs);
		}
	}
}
