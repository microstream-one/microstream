package net.jadoth.test.corp.logic;

import java.io.File;

import net.jadoth.X;
import net.jadoth.chars.VarString;
import net.jadoth.concurrency.XThreads;
import net.jadoth.persistence.internal.PersistenceTypeDictionaryFileHandlerArchiving;
import net.jadoth.storage.types.EmbeddedStorage;
import net.jadoth.storage.types.EmbeddedStorageManager;
import net.jadoth.storage.types.Storage;
import net.jadoth.storage.types.StorageBackupSetup;
import net.jadoth.storage.types.StorageDataFileValidator;
import net.jadoth.storage.types.StorageTransactionsFileAnalysis;


public class MainTestContinousStoring
{
	// creates and start an embedded storage manager with all-default-settings.
	static final EmbeddedStorageManager STORAGE = EmbeddedStorage
		.Foundation(
			Storage.ConfigurationBuilder()
			.setFileEvaluator(
				Storage.DataFileEvaluator(1_000, 10_000, 0.7)
			)
			// (01.03.2019 TM)FIXME: JET-55: build byte-wise comparison of storage files and backup files.
			.setBackupSetup(
				StorageBackupSetup.New(
					Storage
					.FileProviderBuilder()
					.setStorageDirectory("storage/backup")
					.setDeletionDirectory("storage/backup/deleted")
					.setTruncationDirectory("storage/backup/truncated")
					.setFileHandlerCreator(PersistenceTypeDictionaryFileHandlerArchiving::New)
					.createFileProvider()
				)
			)
		)
		.setDataFileValidatorCreator(
			StorageDataFileValidator.CreatorDebugLogging()
		)
		.start()
	;
	
	static Object[] createArray(final int size)
	{
		return X.Array(Object.class, size, i -> "Element" + i);
	}

	public static void main(final String[] args)
	{
//		printTransactionsFiles();
		final Object[] array = createArray(1000);
		STORAGE.root().set(array);
		Test.print("STORAGE: storing ...");
		STORAGE.store(STORAGE.root());
		
		for(int i = 0; i < 10; i++)
		{
			XThreads.sleep(1000);
			STORAGE.store(array);
			STORAGE.issueFullFileCheck();
		}
		XThreads.sleep(2000);
		System.exit(0); // no shutdown required, the storage concept is inherently crash-safe
	}
	
	public static void printTransactionsFiles()
	{
		printTransactionsFiles(1);
	}
	
	public static void printTransactionsFiles(final int channelCount)
	{
		System.out.println(StorageTransactionsFileAnalysis.EntryAssembler.assembleHeader(VarString.New(), "\t"));
		for(int i = 0; i < channelCount; i++)
		{
			final VarString vs = StorageTransactionsFileAnalysis.Logic.parseFile(
				new File("storage/channel_"+i+"/transactions_"+i+".sft")
			);
			System.out.println(vs);
		}
	}
		
}
