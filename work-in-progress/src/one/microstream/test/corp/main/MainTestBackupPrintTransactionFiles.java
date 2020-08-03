package one.microstream.test.corp.main;

import one.microstream.afs.ADirectory;
import one.microstream.afs.AFile;
import one.microstream.chars.VarString;
import one.microstream.storage.types.Storage;
import one.microstream.storage.types.StorageFileNameProvider;
import one.microstream.storage.types.StorageTransactionsAnalysis;


public class MainTestBackupPrintTransactionFiles
{
	// local copy to prevent storage starting caused by referencing a constant in MainTestBackupStoring (crazy! :D)
	static final ADirectory DIRECTORY_STORAGE = Storage.defaultStorageDirectory();
	static final ADirectory DIRECTORY_BACKUP  = DIRECTORY_STORAGE.ensureDirectory("backup");
	
	
	public static void main(final String[] args)
	{
		printTransactionsFiles();
	}
	
	public static void printTransactionsFiles()
	{
		printTransactionsFiles(DIRECTORY_STORAGE, 1);
		printTransactionsFiles(DIRECTORY_BACKUP, 1);
	}
	
	public static void printTransactionsFiles(final ADirectory baseDirectory, final int channelCount)
	{
		System.out.println(baseDirectory);
		System.out.println(StorageTransactionsAnalysis.EntryAssembler.assembleHeader(VarString.New(), "\t"));
		for(int i = 0; i < channelCount; i++)
		{
			final AFile file = baseDirectory.ensureDirectory(StorageFileNameProvider.Defaults.defaultChannelDirectoryPrefix() + i)
				.ensureDirectory(StorageFileNameProvider.Defaults.defaultTransactionsFilePrefix() + i)
			 	.ensureFile(StorageFileNameProvider.Defaults.defaultTransactionsFileSuffix())
			 ;
			
			final VarString vs = StorageTransactionsAnalysis.Logic.parseFile(file);
			System.out.println(vs);
		}
	}
}
