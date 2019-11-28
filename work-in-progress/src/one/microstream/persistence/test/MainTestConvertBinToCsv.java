package one.microstream.persistence.test;

import java.nio.file.Path;
import java.util.function.Predicate;

import one.microstream.X;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.functional.XFunc;
import one.microstream.io.XIO;
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
			X.List(XIO.Path("C:/Files/export/bin/ExportTest.dat")),
			XFunc.all()
		);
	}

	static void convertBinToCsv(
		final XGettingCollection<Path> binaryFiles,
		final Predicate<? super Path> filter
	)
	{
		final StorageDataConverterTypeBinaryToCsv converter = new StorageDataConverterTypeBinaryToCsv.UTF8(
			StorageDataConverterCsvConfiguration.defaultConfiguration(),
			new StorageEntityTypeConversionFileProvider.Default(
				XIO.Path(binaryFiles.get().getParent().getParent(), "csv"), "csv"
			),
			BinaryPersistence.provideTypeDictionaryFromFile(XIO.Path("C:/Files/PersistenceTypeDictionary.ptd")),
			null,
			1<<20,
			1<<20
		);

		for(final Path file : binaryFiles)
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
