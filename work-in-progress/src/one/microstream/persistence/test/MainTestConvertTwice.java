package one.microstream.persistence.test;

import java.nio.file.Path;

import one.microstream.X;
import one.microstream.functional.XFunc;
import one.microstream.io.XPaths;
import one.microstream.storage.types.StorageDataConverterCsvConfiguration;
import one.microstream.storage.types.StorageDataConverterTypeBinaryToCsv;
import one.microstream.storage.types.StorageEntityTypeConversionFileProvider;
import one.microstream.storage.types.StorageLockedFile;

@SuppressWarnings("unused")
public class MainTestConvertTwice extends TestStorage
{
	static final Path   dir      = XPaths.Path("D:/zStorageConversionBug");
	static final String filename = "java.lang.String";

	public static void main(final String[] args)
	{
		final StorageDataConverterTypeBinaryToCsv converter = new StorageDataConverterTypeBinaryToCsv.UTF8(
			StorageDataConverterCsvConfiguration.defaultConfiguration(),
			new StorageEntityTypeConversionFileProvider.Default(XPaths.Path(dir, "csv"), "csv"),
			STORAGE.typeDictionary(),
			null,
			4096,
			4096
		);
		final StorageLockedFile file = StorageLockedFile.openLockedFile(
			XPaths.Path(XPaths.Path(dir, "bin"), filename + ".dat")
		);
		converter.convertDataFile(file);


		final Path bin2Dir = MainTestConvertCsvToBin.convertCsvToBin(
			STORAGE.typeDictionary(),
			X.List(XPaths.Path(XPaths.Path(dir, "csv"), filename + ".csv")),
			XPaths.Path(dir, "bin2"),
			XFunc.all()
		);
	}
}
