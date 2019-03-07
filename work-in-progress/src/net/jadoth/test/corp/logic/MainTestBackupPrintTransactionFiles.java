package net.jadoth.test.corp.logic;

import java.io.File;

import net.jadoth.chars.VarString;
import net.jadoth.storage.types.StorageFileProvider;
import net.jadoth.storage.types.StorageTransactionsFileAnalysis;

public class MainTestBackupPrintTransactionFiles
{
	public static void main(final String[] args)
	{
		printTransactionsFiles();
	}
	
	public static void printTransactionsFiles()
	{
		printTransactionsFiles(MainTestBackupStoring.DIRECTORY_STORAGE, 1);
		printTransactionsFiles(MainTestBackupStoring.DIRECTORY_BACKUP, 1);
	}
	
	public static void printTransactionsFiles(final String baseDirectory, final int channelCount)
	{
		System.out.println(baseDirectory);
		System.out.println(StorageTransactionsFileAnalysis.EntryAssembler.assembleHeader(VarString.New(), "\t"));
		for(int i = 0; i < channelCount; i++)
		{
			final VarString vs = StorageTransactionsFileAnalysis.Logic.parseFile(
				new File(baseDirectory
					+ "/" + StorageFileProvider.Defaults.defaultChannelDirectoryPrefix() + i
					+"/" + StorageFileProvider.Defaults.defaultTransactionFilePrefix() + i
					+ StorageFileProvider.Defaults.defaultTransactionFileSuffix()
				)
			);
			System.out.println(vs);
		}
	}
}
