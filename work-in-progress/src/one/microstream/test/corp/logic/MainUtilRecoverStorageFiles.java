package one.microstream.test.corp.logic;

import java.io.File;

import one.microstream.collections.BulkList;
import one.microstream.persistence.binary.types.BinaryEntityRawDataIterator;
import one.microstream.persistence.types.Persistence;
import one.microstream.storage.types.StorageDataFileValidator;
import one.microstream.storage.types.StorageEntityDataValidator;
import one.microstream.storage.types.StorageFileEntityDataIterator;
import one.microstream.storage.types.StorageFileProvider;
import one.microstream.storage.types.StorageNumberedFile;
import one.microstream.storage.types.StorageTransactionsFile;

public class MainUtilRecoverStorageFiles
{
	static final String PATH_CORRUPTED =
		"D:/_Allianz/2019-03-14_ProdDb/20190313_2330_autobackup_prod_kaputt/backup_daily_2019-03-13Z"
	;
	
	
	public static void main(final String[] args)
	{
		validateDataFiles();
//		printTransactionsFile();
	}
	

	private static void validateDataFiles()
	{
		final StorageFileProvider sfp = StorageFileProvider
			.Builder()
			.setBaseDirectory(PATH_CORRUPTED)
			.createFileProvider()
		;
		final BulkList<StorageNumberedFile> storageFiles = sfp.collectDataFiles(BulkList.New(), 0);
		
		final StorageDataFileValidator dfv = StorageDataFileValidator.DebugLogging(
			BinaryEntityRawDataIterator.New(),
			StorageEntityDataValidator.DebugLogging(
				StorageEntityDataValidator.New(
					24, 100_000,
					 1, 1013002,
					 Persistence.defaultStartObjectId(), Persistence.defaultBoundConstantId()
				)
			),
			StorageFileEntityDataIterator.New()
		);
		
		for(final StorageNumberedFile file : storageFiles)
		{
			dfv.validateFile(file);
		}
	}
	
	private static void printTransactionsFile()
	{
		final StorageTransactionsFile tf = StorageTransactionsFile.parseFile(
			new File(PATH_CORRUPTED + "/channel_0/transactions_0.sft")
		);
		tf.entries().iterate(System.out::println);
	}
	
}
