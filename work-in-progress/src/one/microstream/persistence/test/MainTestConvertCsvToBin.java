package one.microstream.persistence.test;

import java.util.function.Predicate;

import one.microstream.X;
import one.microstream.afs.ADirectory;
import one.microstream.afs.AFS;
import one.microstream.afs.AFile;
import one.microstream.afs.nio.NioFileSystem;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.functional.XFunc;
import one.microstream.io.XIO;
import one.microstream.persistence.binary.types.BinaryPersistence;
import one.microstream.persistence.types.PersistenceTypeDictionary;
import one.microstream.storage.types.StorageDataConverterCsvConfiguration;
import one.microstream.storage.types.StorageDataConverterTypeCsvToBinary;
import one.microstream.storage.types.StorageEntityTypeConversionFileProvider;

public class MainTestConvertCsvToBin
{
	public static void main(final String[] args)
	{
		final NioFileSystem nfs = NioFileSystem.New();
		convertCsvToBin(
			BinaryPersistence.provideTypeDictionaryFromFile(nfs.ensureFile(XIO.Path("C:/Files/PersistenceTypeDictionary.ptd"))),
			X.List(nfs.ensureFile(XIO.Path("C:/Files/export/csv/ExportTest.csv"))),
			nfs.ensureDirectory(XIO.Path("C:/Files/export/bin2")),
			XFunc.all()
		);
	}

	static ADirectory convertCsvToBin(
		final PersistenceTypeDictionary typeDictionary ,
		final XGettingCollection<AFile> binaryFiles    ,
		final ADirectory                targetDirectory,
		final Predicate<? super AFile>  filter
	)
	{
		final StorageDataConverterTypeCsvToBinary<AFile> converter = StorageDataConverterTypeCsvToBinary.New(
			StorageDataConverterCsvConfiguration.defaultConfiguration(),
			typeDictionary,
			new StorageEntityTypeConversionFileProvider.Default(
				targetDirectory, "dat"
			)
		);
		
		for(final AFile file : binaryFiles)
		{
			if(!filter.test(file))
			{
				continue;
			}
			
			AFS.execute(file, rf -> converter.convertCsv(rf));
		}

		
		return targetDirectory;
	}

}
