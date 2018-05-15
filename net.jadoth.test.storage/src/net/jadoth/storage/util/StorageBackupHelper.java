package net.jadoth.storage.util;

import java.io.File;

import net.jadoth.chars.VarString;
import net.jadoth.persistence.internal.AbstractIdProviderByFile;
import net.jadoth.persistence.internal.PersistenceTypeDictionaryFileHandler;
import net.jadoth.persistence.types.PersistenceTypeDictionary;
import net.jadoth.persistence.types.PersistenceTypeDictionaryAssembler;
import net.jadoth.storage.types.EmbeddedStorageConnectionFoundation;
import net.jadoth.storage.types.EmbeddedStorageFoundation;
import net.jadoth.storage.types.EmbeddedStorageManager;
import net.jadoth.storage.types.Storage;
import net.jadoth.storage.types.StorageFileWriter;
import net.jadoth.storage.types.StorageIoHandler;


public final class StorageBackupHelper
{
	// (30.06.2016 TM)TODO: https://www.xdevissues.com/browse/OGS-21

	public static void backup(
		final EmbeddedStorageManager    storageManager   ,
		final EmbeddedStorageFoundation storageFoundation,
		final File                      targetDirectory
	)
	{
		backupData(storageManager, targetDirectory);
		backupMetadata(storageFoundation.getConnectionFoundation(), targetDirectory);
	}

	static void backupData(final EmbeddedStorageManager storageManager, final File targetDirectory)
	{
		// export (= copy) all channels' data to the target directory (= "backup")
		storageManager.exportChannels(
			new StorageIoHandler.Implementation(
				Storage.FileProvider(targetDirectory),
				new StorageFileWriter.Implementation()
			),
			true
		);
	}

	static void backupMetadata(final EmbeddedStorageConnectionFoundation connectionFoundation, final File targetDirectory)
	{
		// Der erste Teil nervt mich noch. Darum: https://www.xdevissues.com/browse/OGS-21
		final PersistenceTypeDictionaryAssembler  dictionaryAssembler = connectionFoundation.getTypeDictionaryAssembler();
		final PersistenceTypeDictionary           typeDictionary      = connectionFoundation.getTypeDictionaryManager().provideTypeDictionary();

		final long   nextObjectId   = connectionFoundation.getObjectIdProvider().currentObjectId() + 1;
		final long   nextTypeId     = typeDictionary.determineHighestTypeId() + 1;
		final String typeDictString = dictionaryAssembler.appendTypeDictionary(VarString.New(), typeDictionary).toString();

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
