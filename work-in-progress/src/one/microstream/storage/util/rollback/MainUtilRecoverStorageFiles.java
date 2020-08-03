package one.microstream.storage.util.rollback;

import static one.microstream.X.KeyValue;

import one.microstream.afs.ADirectory;
import one.microstream.afs.AFS;
import one.microstream.afs.AFile;
import one.microstream.afs.nio.NioFileSystem;
import one.microstream.collections.BulkList;
import one.microstream.collections.EqHashTable;
import one.microstream.io.XIO;
import one.microstream.meta.XDebug;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryEntityRawDataIterator;
import one.microstream.persistence.types.Persistence;
import one.microstream.storage.types.StorageDataFileValidator;
import one.microstream.storage.types.StorageDataInventoryFile;
import one.microstream.storage.types.StorageEntityDataValidator;
import one.microstream.storage.types.StorageFileEntityDataIterator;
import one.microstream.storage.types.StorageLiveFileProvider;
import one.microstream.storage.types.StorageTransactionsEntries;

public class MainUtilRecoverStorageFiles
{
	static final ADirectory PATH_CORRUPTED = NioFileSystem.New().ensureDirectory(XIO.Path(
		"D:/_Corp/2019-01-10/db_lcm_prod_v400"
	));
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
		final StorageLiveFileProvider sfp = StorageLiveFileProvider
			.Builder()
			.setDirectory(PATH_CORRUPTED)
			.createFileProvider()
		;
		final BulkList<StorageDataInventoryFile> storageFiles = sfp.collectDataFiles(
			StorageDataInventoryFile::New,
			BulkList.New(),
			0
		);
		
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
		
		for(final StorageDataInventoryFile file : storageFiles)
		{
			dfv.validateFile(file);
		}
	}
	
	static void printTransactionsFile()
	{
		final StorageTransactionsEntries tf = StorageTransactionsEntries.parseFile(
			PATH_CORRUPTED.ensureDirectory("channel_0").ensureFile("transactions_0.sft")
		);
		tf.entries().iterate(System.out::println);
	}
	
	static void rollbackTransfers() throws Exception
	{
		final AFile sourceFile = PATH_CORRUPTED.ensureDirectory("channel_0").ensureFile("channel_0_491.dat");
				
		final ADirectory dir = AFS.ensureExists(PATH_CORRUPTED.ensureDirectory("strings"));
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
