package one.microstream.storage.util;

import one.microstream.afs.ADirectory;
import one.microstream.afs.AFile;
import one.microstream.chars.VarString;
import one.microstream.persistence.internal.PersistenceTypeDictionaryFileHandler;
import one.microstream.persistence.types.PersistenceTypeDictionary;
import one.microstream.persistence.types.PersistenceTypeDictionaryAssembler;
import one.microstream.storage.types.EmbeddedStorageConnectionFoundation;
import one.microstream.storage.types.EmbeddedStorageFoundation;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.storage.types.StorageLiveFileProvider;


public final class StorageBackupHelper
{
	/* (07.10.2019 TM)NOTE: priv#150
	 * On a technical level, this type of backup creation has actually been completely replaced
	 * by the continuous backup functionality (see StorageConfiguration#backupSetup).
	 * 
	 * However, it turned out that on a psychological level, customers still want a "full backup" functionality
	 * because they do not fully trust a continous backup. The absence of problems plays no role in this,
	 * they simply want a "backup everything now" command.
	 * So instead of copying only new files as they get created and filled, there's still need for a variant
	 * that copies ALL files, no matter how inefficient, redundant or unnecessary that is.
	 */

	public static void backup(
		final EmbeddedStorageManager       storageManager        ,
		final EmbeddedStorageFoundation<?> storageFoundation     ,
		final ADirectory                   targetDirectory       ,
		final String                       typeDictionaryFileName
	)
	{
		backup(storageManager, storageFoundation.getConnectionFoundation(), targetDirectory, typeDictionaryFileName);
	}
	
	public static void backup(
		final EmbeddedStorageManager                 storageManager        ,
		final EmbeddedStorageConnectionFoundation<?> connectionFoundation  ,
		final ADirectory                             targetDirectory       ,
		final String                                 typeDictionaryFileName
	)
	{
		// (07.10.2019 TM)NOTE: GC deactivated until further notice, so this flag has no effect.
		backupData(storageManager, targetDirectory, true);
		backupMetadata(connectionFoundation, targetDirectory, typeDictionaryFileName);
	}

	static void backupData(
		final EmbeddedStorageManager storageManager          ,
		final ADirectory             targetDirectory         ,
		final boolean                runFullGarbageCollection
	)
	{
		// export (= copy) all channels' data files to the target directory (= "create backup")
		storageManager.exportChannels(
			StorageLiveFileProvider.New(targetDirectory),
			runFullGarbageCollection
		);
	}

	static void backupMetadata(
		final EmbeddedStorageConnectionFoundation<?> connectionFoundation  ,
		final ADirectory                             targetDirectory       ,
		final String                                 typeDictionaryFileName
	)
	{
		// this initial part ist still a bit annoying. Could be improved.
		final PersistenceTypeDictionaryAssembler  dictionaryAssembler = connectionFoundation.getTypeDictionaryAssembler();
		final PersistenceTypeDictionary           typeDictionary      = connectionFoundation.getTypeDictionaryManager().provideTypeDictionary();

		// (07.10.2019 TM)NOTE: the ID-files have long become obsolete since the values are now determined dynamically
//		final long   nextObjectId   = connectionFoundation.getObjectIdProvider().currentObjectId() + 1;
//		final long   nextTypeId     = typeDictionary.determineHighestTypeId() + 1;
		final String typeDictString = dictionaryAssembler.assemble(VarString.New(), typeDictionary).toString();

		// arbitrary file names, preferably the same that were used for creating the EmbeddedStorageConnectionFoundation instance.
//		final AFile fileOid = targetDirectory.ensureFile("MyObjectId.oid");
//		final AFile fileTid = targetDirectory.ensureFile("MyTypeId.oid");
		final AFile fileTDc = targetDirectory.ensureFile(typeDictionaryFileName);

		// write current metadata's state to the specified files (= "metadata backup")
		PersistenceTypeDictionaryFileHandler.writeTypeDictionary(fileTDc, typeDictString);
//		AbstractIdProviderByFile            .writeId            (fileTid, nextTypeId    );
//		AbstractIdProviderByFile            .writeId            (fileOid, nextObjectId  );
	}

	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException
	 */
	private StorageBackupHelper()
	{
		// static only
		throw new UnsupportedOperationException();
	}

}
