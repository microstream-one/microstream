package one.microstream.storage.util;

import java.io.File;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

import one.microstream.collections.BulkList;
import one.microstream.io.XPaths;
import one.microstream.storage.types.EmbeddedStorageManager;
import one.microstream.storage.types.StorageConnection;
import one.microstream.storage.types.StorageDataConverterCsvConfiguration;
import one.microstream.storage.types.StorageDataConverterTypeBinaryToCsv;
import one.microstream.storage.types.StorageEntityTypeConversionFileProvider;
import one.microstream.storage.types.StorageEntityTypeExportFileProvider;
import one.microstream.storage.types.StorageEntityTypeExportStatistics;
import one.microstream.storage.types.StorageEntityTypeHandler;
import one.microstream.storage.types.StorageLockedFile;
import one.microstream.storage.types.StorageTypeDictionary;


/**
 * Inofficial helper-util class to export the content of a OGS database as CSV.
 * <br>
 * <br>
 * Siehe<br>
 * {@link UtilStorageCsvExport#exportCsv(EmbeddedStorageManager, File)}<br>
 * {@link UtilStorageCsvExport#exportCsv(EmbeddedStorageManager, File, Predicate)}<br>
 * 
 * @author TM
 */
public class UtilStorageCsvExport
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////
	
	static final int    BUFFER_SIZE_READ  =  4096;
	static final int    BUFFER_SIZE_WRITE =  4096;
	static final String FILE_SUFFIX_BIN   = "dat";
	static final String FILE_SUFFIX_CSV   = "csv";
	static final String SUB_DIRECTORY_BIN = "bin";
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	/**
	 * Exportiert die von dem übergebenenen {@link EmbeddedStorageManager} repräsentierte Datenbank in Binary Dateien
	 * je Typ und konvertiert diese anschließend in das CSV-Format.<br>
	 * Mit {@link UtilStorageCsvExport#exportCsv(EmbeddedStorageManager, File, Predicate)} können Typen gefiltert werden.
	 * 
	 * @param storage Die zu exportierende Datenbank.
	 * @param targetDirectory Das Zielverzeichnis für die CSV-Dateien.
	 * @return Die Statistik der OGS Binary Export Funktion, siehe {@link StorageEntityTypeExportStatistics}.
	 * @see UtilStorageCsvExport#exportCsv(EmbeddedStorageManager, File, Predicate)
	 */
	public static StorageEntityTypeExportStatistics exportCsv(
		final EmbeddedStorageManager storage        ,
		final Path                   targetDirectory
	)
	{
		return exportCsv(storage, targetDirectory, null);
	}
		
	/**
	 * Variante von {@link UtilStorageCsvExport#exportCsv(EmbeddedStorageManager, File)} mit optionaler Filterlogik.
	 * 
	 * @param storage Die zu exportierende Datenbank.
	 * @param targetDirectory Das Zielverzeichnis für die CSV-Dateien.
	 * @param exportTypeFilter Eine beliebige Filterlogik für in der Datenbank vorhandene Typen. Kann {@code null} sein.
	 * @return Die Statistik der OGS Binary Export Funktion, siehe {@link StorageEntityTypeExportStatistics}.
	 * @see UtilStorageCsvExport#exportCsv(EmbeddedStorageManager, File)
	 */
	public static StorageEntityTypeExportStatistics exportCsv(
		final EmbeddedStorageManager                      storage         ,
		final Path                                        targetDirectory ,
		final Predicate<? super StorageEntityTypeHandler> exportTypeFilter
	)
	{
		return internalExportBinaryAndConvert(storage, targetDirectory, exportTypeFilter);
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// internals //
	//////////////
	
	static StorageEntityTypeExportStatistics internalExportBinaryAndConvert(
		final EmbeddedStorageManager                      storage        ,
		final Path                                        targetDirectory,
		final Predicate<? super StorageEntityTypeHandler> exportFilter
	)
	{
		final Path binDirectory = XPaths.ensureDirectoryUnchecked(XPaths.Path(targetDirectory, SUB_DIRECTORY_BIN));

		final BulkList<Path> exportFiles = BulkList.New(1000);
		
		final long tStart = System.nanoTime();
		final StorageEntityTypeExportStatistics result = internalExportTypes(
			storage,
			t ->
				!t.isPrimitiveType()
				&& (exportFilter == null || exportFilter.test(t)),
			binDirectory,
			exportFiles,
			FILE_SUFFIX_BIN
		);
		final long tStop = System.nanoTime();
		System.out.println("Binary export completed. Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));

		internalConvertToCsv(storage, targetDirectory, exportFiles.iterator(), FILE_SUFFIX_BIN);
		
		return result;
	}
	
	static final StorageEntityTypeExportStatistics internalExportTypes(
		final StorageConnection                           storageConnection  ,
		final Predicate<? super StorageEntityTypeHandler> isExportType       ,
		final Path                                        targetDirectory    ,
		final Consumer<? super Path>                      exportFileCollector,
		final String                                      fileSuffix
	)
	{
		final StorageEntityTypeExportStatistics result = storageConnection.exportTypes(
			new StorageEntityTypeExportFileProvider.Default(targetDirectory, fileSuffix),
			isExportType
		);
		System.out.println(result);

		
		result.typeStatistics().values().iterate(s ->
			exportFileCollector.accept(
				XPaths.Path(s.file().identifier())
			)
		);
		
		return result;
	}
	
	static Path internalConvertToCsv(
		final EmbeddedStorageManager storage     ,
		final Path                   csvDirectory,
		final Iterator<Path>         binaryFiles ,
		final String                 fileSuffix
	)
	{
		final String effectiveFileSuffix = "." + fileSuffix;
		
		final Predicate<Path> filter = file ->
			!XPaths.isDirectoryUnchecked(file)
			&& XPaths.getFileName(file).endsWith(effectiveFileSuffix)
		;
		
		final long tStart = System.nanoTime();
		internalConvertFiles(storage.typeDictionary(), csvDirectory, binaryFiles, filter, null);
		final long tStop = System.nanoTime();
		System.out.println("CSV conversion completed. Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));

		return csvDirectory;
	}
	
	static void internalConvertFiles(
		final StorageTypeDictionary   typeDictionary    ,
		final Path                    csvTargetDirectory,
		final Iterator<Path>          fileProvider      ,
		final Predicate<? super Path> filter            ,
		final String                  name
	)
	{
		final StorageDataConverterTypeBinaryToCsv converter = new StorageDataConverterTypeBinaryToCsv.UTF8(
			StorageDataConverterCsvConfiguration.defaultConfiguration(),
			new StorageEntityTypeConversionFileProvider.Default(csvTargetDirectory, FILE_SUFFIX_CSV),
			typeDictionary,
			null,
			BUFFER_SIZE_READ,
			BUFFER_SIZE_WRITE
		);

		while(true)
		{
			final Path file;
			
			// fileProvider is queried by multiple threads and must therefore be used in a synchronized fashion.
			synchronized(fileProvider)
			{
				if(fileProvider.hasNext())
				{
					file = fileProvider.next();
				}
				else
				{
					break;
				}
			}
			
			if(!filter.test(file))
			{
				printAction(name, "skipping", file);
				continue;
			}
			
			printAction(name, "converting", file);
			try
			{
				final StorageLockedFile storageFile = StorageLockedFile.openLockedFile(file);
				converter.convertDataFile(storageFile);
			}
			catch(final Exception e)
			{
				throw new RuntimeException("Exception while converting file " + file, e);
			}
		}
	}
	
	static void printAction(final String name, final String action, final Path file)
	{
		System.out.println((name == null ? "" : name + " ") + action + " " + XPaths.getFilePath(file));
	}
	
	static final File ensureDirectory(final File directory)
	{
		try
		{
			if(directory.exists())
			{
				return directory;
			}
			
			synchronized(directory)
			{
				if(!directory.mkdirs())
				{
					// check again in case it has been created in the meantime (race condition)
					if(!directory.exists())
					{
						throw new RuntimeException("Directory could not have been created: " + directory);
					}
				}
			}
		}
		catch(final SecurityException e)
		{
			throw new RuntimeException("Security Exception for directory " + directory, e);
		}

		return directory;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// test //
	/////////
	
	public static void main(final String[] args)
	{
		// the instance must come from somewhere in the application logic, where the storage has been initialized.
		final EmbeddedStorageManager storage = null; // FIXME: EmbeddedStorageManager Instanz der Anwendung.
		
		// export all
		UtilStorageCsvExport.exportCsv(
			storage,
			XPaths.Path("C:/StorageExportTest_2018-02-20-1600_ALL")
		);
		
		// Export-Type-Filter example: only export Strings
		UtilStorageCsvExport.exportCsv(
			storage,
			XPaths.Path("C:/StorageExportTest_2018-02-20-1600_Strings"),
			t -> t.typeName().equals(String.class.getName())
		);
		
		System.exit(0);
	}
		
}
