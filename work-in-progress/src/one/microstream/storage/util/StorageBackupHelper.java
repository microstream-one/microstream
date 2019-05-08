package one.microstream.storage.util;

import java.io.File;

import one.microstream.chars.VarString;
import one.microstream.persistence.internal.AbstractIdProviderByFile;
import one.microstream.persistence.internal.PersistenceTypeDictionaryFileHandler;
import one.microstream.persistence.types.PersistenceTypeDictionary;
import one.microstream.persistence.types.PersistenceTypeDictionaryAssembler;
import one.microstream.storage.types.EmbeddedStorageConnectionFoundation;
import one.microstream.storage.types.EmbeddedStorageFoundation;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.storage.types.Storage;
import one.microstream.storage.types.StorageFileWriter;
import one.microstream.storage.types.StorageIoHandler;


public final class StorageBackupHelper
{
	// (30.06.2016 TM)TODO: https://www.xdevissues.com/browse/OGS-21

	public static void backup(
		final EmbeddedStorageManager       storageManager   ,
		final EmbeddedStorageFoundation<?> storageFoundation,
		final File                         targetDirectory
	)
	{
		backupData(storageManager, targetDirectory);
		backupMetadata(storageFoundation.getConnectionFoundation(), targetDirectory);
	}

	static void backupData(final EmbeddedStorageManager storageManager, final File targetDirectory)
	{
		// export (= copy) all channels' data to the target directory (= "backup")
		storageManager.exportChannels(
			new StorageIoHandler.Default(
				Storage.FileProvider(targetDirectory),
				new StorageFileWriter.Default()
			),
			true
		);
	}

	static void backupMetadata(
		final EmbeddedStorageConnectionFoundation<?> connectionFoundation,
		final File                                   targetDirectory
	)
	{
		// Der erste Teil nervt mich noch. Darum: https://www.xdevissues.com/browse/OGS-21
		final PersistenceTypeDictionaryAssembler  dictionaryAssembler = connectionFoundation.getTypeDictionaryAssembler();
		final PersistenceTypeDictionary           typeDictionary      = connectionFoundation.getTypeDictionaryManager().provideTypeDictionary();

		final long   nextObjectId   = connectionFoundation.getObjectIdProvider().currentObjectId() + 1;
		final long   nextTypeId     = typeDictionary.determineHighestTypeId() + 1;
		final String typeDictString = dictionaryAssembler.assemble(VarString.New(), typeDictionary).toString();

		// arbitrary file names, preferably the same that were used for creating the EmbeddedStorageConnectionFoundation instance.
		final File fileOid = new File(targetDirectory, "MyObjectId.oid");
		final File fileTid = new File(targetDirectory, "MyTypeId.oid");
		final File fileTDc = new File(targetDirectory, "MyPersistenceTypeDictionary.ptd");

		// write current metadata's state to the specified files (= "metadata backup")
		PersistenceTypeDictionaryFileHandler.writeTypeDictionary(fileTDc, typeDictString);
		AbstractIdProviderByFile            .writeId            (fileTid, nextTypeId    );
		AbstractIdProviderByFile            .writeId            (fileOid, nextObjectId  );
	}



	private StorageBackupHelper()
	{
		// static only
		throw new UnsupportedOperationException();
	}

}
