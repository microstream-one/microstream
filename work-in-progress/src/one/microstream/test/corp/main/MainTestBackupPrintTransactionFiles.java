package one.microstream.test.corp.main;

import one.microstream.chars.VarString;
import one.microstream.io.XIO;
import one.microstream.storage.types.StorageFileNameProvider;
import one.microstream.storage.types.StorageLiveFileProvider;
import one.microstream.storage.types.StorageTransactionsAnalysis;

public class MainTestBackupPrintTransactionFiles
{
	// local copy to prevent storage starting caused by referencing a constant in MainTestBackupStoring (crazy! :D)
	static final String DIRECTORY_STORAGE = StorageLiveFileProvider.Defaults.defaultStorageDirectory();
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
		System.out.println(StorageTransactionsAnalysis.EntryAssembler.assembleHeader(VarString.New(), "\t"));
		for(int i = 0; i < channelCount; i++)
		{
			final VarString vs = StorageTransactionsAnalysis.Logic.parseFile(
				XIO.Path(baseDirectory
					+ "/" + StorageFileNameProvider.Defaults.defaultChannelDirectoryPrefix() + i
					+"/" + StorageFileNameProvider.Defaults.defaultTransactionsFilePrefix() + i
					+ StorageFileNameProvider.Defaults.defaultTransactionsFileSuffix()
				)
			);
			System.out.println(vs);
		}
	}
}
