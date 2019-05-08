package one.microstream.persistence.test;

import java.io.File;
import java.util.function.Predicate;

import one.microstream.X;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.functional.XFunc;
import one.microstream.persistence.binary.types.BinaryPersistence;
import one.microstream.storage.types.StorageDataConverterCsvConfiguration;
import one.microstream.storage.types.StorageDataConverterTypeBinaryToCsv;
import one.microstream.storage.types.StorageEntityTypeConversionFileProvider;
import one.microstream.storage.types.StorageLockedFile;

public class MainTestConvertBinToCsv
{
	public static void main(final String[] args)
	{
		convertBinToCsv(
			X.List(new File("C:/Files/export/bin/ExportTest.dat")),
			XFunc.all()
		);
	}

	static void convertBinToCsv(
		final XGettingCollection<File> binaryFiles,
		final Predicate<? super File> filter
	)
	{
		final StorageDataConverterTypeBinaryToCsv converter = new StorageDataConverterTypeBinaryToCsv.UTF8(
			StorageDataConverterCsvConfiguration.defaultConfiguration(),
			new StorageEntityTypeConversionFileProvider.Default(
				new File(binaryFiles.get().getParentFile().getParentFile(), "csv"), "csv"
			),
			BinaryPersistence.provideTypeDictionaryFromFile(new File("C:/Files/PersistenceTypeDictionary.ptd")),
			null,
			1<<20,
			1<<20
		);

		for(final File file : binaryFiles)
		{
			if(!filter.test(file))
			{
				continue;
			}
			
			final StorageLockedFile storageFile = StorageLockedFile.openLockedFile(file);
			converter.convertDataFile(storageFile);
		}
	}

}
