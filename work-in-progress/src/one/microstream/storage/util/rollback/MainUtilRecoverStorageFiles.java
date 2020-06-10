package one.microstream.storage.util.rollback;

import static one.microstream.X.KeyValue;

import java.nio.file.Path;

import one.microstream.collections.BulkList;
import one.microstream.collections.EqHashTable;
import one.microstream.io.XIO;
import one.microstream.meta.XDebug;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryEntityRawDataIterator;
import one.microstream.persistence.types.Persistence;
import one.microstream.storage.types.StorageDataFileValidator;
import one.microstream.storage.types.StorageEntityDataValidator;
import one.microstream.storage.types.StorageFileEntityDataIterator;
import one.microstream.storage.types.StorageFileProvider;
import one.microstream.storage.types.StorageTransactionsEntries;
import one.microstream.storage.types.ZStorageNumberedFile;

public class MainUtilRecoverStorageFiles
{
	static final String PATH_CORRUPTED =
		"D:/_Corp/2019-01-10/db_lcm_prod_v400"
	;
	static final long
		LENGTH_LOWER_VALUE   = Binary.entityHeaderLength()       ,
		LENGTH_UPPER_BOUND   = 100_000                           ,
		TYPEID_LOWER_VALUE   = 10                                ,
		TYPEID_UPPER_BOUND   = 1013002                           ,
		OBJECTID_LOWER_VALUE = Persistence.defaultStartObjectId(),
		OBJECTID_UPPER_BOUND = 1000000000001000000L
	;
	
	public static void main(final String[] args) throws Exception
	{
		rollbackTransfers();
//		validateDataFiles();
//		printTransactionsFile();
	}
//	1000000000000087529
	static void validateDataFiles()
	{
		final StorageFileProvider sfp = StorageFileProvider
			.Builder()
			.setBaseDirectory(PATH_CORRUPTED)
			.createFileProvider()
		;
		final BulkList<ZStorageNumberedFile> storageFiles = sfp.collectDataFiles(BulkList.New(), 0);
		
		final StorageDataFileValidator dfv = StorageDataFileValidator.DebugLogging(
			BinaryEntityRawDataIterator.New(),
			StorageEntityDataValidator.DebugLogging(
				StorageEntityDataValidator.New(
					LENGTH_LOWER_VALUE  , LENGTH_UPPER_BOUND  ,
					TYPEID_LOWER_VALUE  , TYPEID_UPPER_BOUND  ,
					OBJECTID_LOWER_VALUE, OBJECTID_UPPER_BOUND
				)
			),
			StorageFileEntityDataIterator.New()
		);
		
		for(final ZStorageNumberedFile file : storageFiles)
		{
			dfv.validateFile(file);
		}
	}
	
	static void printTransactionsFile()
	{
		final StorageTransactionsEntries tf = StorageTransactionsEntries.parseFile(
			XIO.Path(PATH_CORRUPTED + "/channel_0/transactions_0.sft")
		);
		tf.entries().iterate(System.out::println);
	}
	
	static void rollbackTransfers() throws Exception
	{
		final Path sourceFile = XIO.Path(PATH_CORRUPTED + "/channel_0/channel_0_491.dat");
				
		final Path dir = XIO.unchecked.ensureDirectory(XIO.Path(PATH_CORRUPTED, "strings"));
		XDebug.deleteAllFiles(dir, false);
		
		// 2019-03-13 (2019-03-14)
//		final StorageRollbacker sr = new StorageRollbacker(
//			863,
//			EqHashTable.New(KeyValue(872L, sourceFile)),
//			dir, "rolledBack_", "storesOnly_",
//			new StorageRollbacker.EntityDataHeaderEvaluator(
//				LENGTH_LOWER_VALUE  , LENGTH_UPPER_BOUND  ,
//				TYPEID_LOWER_VALUE  , TYPEID_UPPER_BOUND  ,
//				OBJECTID_LOWER_VALUE, OBJECTID_UPPER_BOUND
//			)
//		);
		
		// 2019-01-10 (2019-04-05)
		final StorageRollbacker sr = new StorageRollbacker(
			490L,
			EqHashTable.New(KeyValue(491L, sourceFile)),
			dir, "rolledBack_", "storesOnly_",
			new StorageRollbacker.EntityDataHeaderEvaluator(
				LENGTH_LOWER_VALUE  , LENGTH_UPPER_BOUND  ,
				TYPEID_LOWER_VALUE  , TYPEID_UPPER_BOUND  ,
				OBJECTID_LOWER_VALUE, OBJECTID_UPPER_BOUND
			)
		);

//		final StorageTransactionsFile tf = StorageTransactionsFile.parseFile(
//			new File(PATH_CORRUPTED + "/channel_0/transactions_0.sft")
//		);
//		sr.rollbackTransfers(tf);
		
//		sr.cleanUpDirect();
		sr.recoverStringsAndPrint();
	}
	
}
