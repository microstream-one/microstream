package one.microstream.persistence.test;

import java.util.function.Predicate;

import one.microstream.X;
import one.microstream.afs.AFS;
import one.microstream.afs.AFile;
import one.microstream.afs.nio.NioFileSystem;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.functional.XFunc;
import one.microstream.io.XIO;
import one.microstream.persistence.binary.types.BinaryPersistence;
import one.microstream.storage.types.StorageDataConverterCsvConfiguration;
import one.microstream.storage.types.StorageDataConverterTypeBinaryToCsv;
import one.microstream.storage.types.StorageEntityTypeConversionFileProvider;

public class MainTestConvertBinToCsv
{
	public static void main(final String[] args)
	{
		convertBinToCsv(
			X.List(NioFileSystem.file(XIO.Path("C:/Files/export/bin/ExportTest.dat"))),
			XFunc.all()
		);
	}

	static void convertBinToCsv(
		final XGettingCollection<AFile> binaryFiles,
		final Predicate<? super AFile>  filter
	)
	{
		final StorageDataConverterTypeBinaryToCsv converter = new StorageDataConverterTypeBinaryToCsv.UTF8(
			StorageDataConverterCsvConfiguration.defaultConfiguration(),
			new StorageEntityTypeConversionFileProvider.Default(
				binaryFiles.get().parent().parent().ensureDirectory("csv"), "csv"
			),
			BinaryPersistence.provideTypeDictionaryFromFile(
				NioFileSystem.file(
					XIO.Path("C:/Files/PersistenceTypeDictionary.ptd")
				)
			),
			null,
			1<<20,
			1<<20
		);

		for(final AFile file : binaryFiles)
		{
			if(!filter.test(file))
			{
				continue;
			}
			
			AFS.execute(file, rf -> converter.convertDataFile(rf));
		}
	}

}
