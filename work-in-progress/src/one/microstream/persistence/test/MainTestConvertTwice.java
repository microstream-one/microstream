package one.microstream.persistence.test;

import one.microstream.X;
import one.microstream.afs.ADirectory;
import one.microstream.afs.AFS;
import one.microstream.afs.nio.NioFileSystem;
import one.microstream.functional.XFunc;
import one.microstream.storage.types.StorageDataConverterCsvConfiguration;
import one.microstream.storage.types.StorageDataConverterTypeBinaryToCsv;
import one.microstream.storage.types.StorageEntityTypeConversionFileProvider;

@SuppressWarnings("unused")
public class MainTestConvertTwice extends TestStorage
{
	static final ADirectory dir      = NioFileSystem.New().ensureDirectoryPath("D:/zStorageConversionBug");
	static final String     filename = "java.lang.String";

	public static void main(final String[] args)
	{
		final StorageDataConverterTypeBinaryToCsv converter = new StorageDataConverterTypeBinaryToCsv.UTF8(
			StorageDataConverterCsvConfiguration.defaultConfiguration(),
			new StorageEntityTypeConversionFileProvider.Default(dir.ensureDirectory("csv"), "csv"),
			STORAGE.typeDictionary(),
			null,
			4096,
			4096
		);
		
		AFS.execute(dir.ensureDirectory("bin").ensureFile(filename, "dat"), rf ->
			converter.convertDataFile(rf)
		);

		final ADirectory bin2Dir = MainTestConvertCsvToBin.convertCsvToBin(
			STORAGE.typeDictionary(),
			X.List(dir.ensureDirectory("csv").ensureFile(filename, "csv")),
			dir.ensureDirectory("bin2"),
			XFunc.all()
		);
	}
}
