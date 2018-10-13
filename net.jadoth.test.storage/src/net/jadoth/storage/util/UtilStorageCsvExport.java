package net.jadoth.storage.util;

import java.io.File;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.jadoth.collections.BulkList;
import net.jadoth.storage.types.EmbeddedStorageManager;
import net.jadoth.storage.types.StorageConnection;
import net.jadoth.storage.types.StorageDataConverterCsvConfiguration;
import net.jadoth.storage.types.StorageDataConverterTypeBinaryToCsv;
import net.jadoth.storage.types.StorageEntityTypeConversionFileProvider;
import net.jadoth.storage.types.StorageEntityTypeExportFileProvider;
import net.jadoth.storage.types.StorageEntityTypeExportStatistics;
import net.jadoth.storage.types.StorageEntityTypeHandler;
import net.jadoth.storage.types.StorageLockedFile;
import net.jadoth.storage.types.StorageTypeDictionary;


/**
 * Inoffizielle Helfer-Util-Klasse, um den Inhalt einer OGS Datenbank als CSV zu exportieren.
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
	// constants        //
	/////////////////////
	
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
	public static StorageEntityTypeExportStatistics exportCsv(final EmbeddedStorageManager storage, final File targetDirectory)
	{
		return exportCsv(storage, targetDirectory, null);
	}
	
	/**
	 * Variante von {@link UtilStorageCsvExport#exportCsv(EmbeddedStorageManager, File)} mit optionaler Filterlogik.
	 * 
	 * @param storage Die zu exportierende Datenbank.
	 * @param targetDirectory Das Zielverzeichnis für die CSV-Dateien.
	 * @param exportTypeFilter Eine beliebige Filterlogik für in der Datenbank vorhandene Typen. Kann <code>null</code> sein.
	 * @return Die Statistik der OGS Binary Export Funktion, siehe {@link StorageEntityTypeExportStatistics}.
	 * @see UtilStorageCsvExport#exportCsv(EmbeddedStorageManager, File)
	 */
	public static StorageEntityTypeExportStatistics exportCsv(
		final EmbeddedStorageManager                      storage         ,
		final File                                        targetDirectory ,
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
		final File                                        targetDirectory,
		final Predicate<? super StorageEntityTypeHandler> exportFilter
	)
	{
		final File binDirectory = ensureDirectory(new File(targetDirectory, SUB_DIRECTORY_BIN));
		final BulkList<File> exportFiles = BulkList.New(1000);
		
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
		final File                                        targetDirectory    ,
		final Consumer<? super File>                      exportFileCollector,
		final String                                      fileSuffix
	)
	{
		final StorageEntityTypeExportStatistics result = storageConnection.exportTypes(
			new StorageEntityTypeExportFileProvider.Implementation(targetDirectory, fileSuffix),
			isExportType
		);
		System.out.println(result);

		
		result.typeStatistics().values().iterate(s ->
			exportFileCollector.accept(
				new File(s.file().identifier())
			)
		);
		
		return result;
	}
	
	static File internalConvertToCsv(
		final EmbeddedStorageManager storage     ,
		final File                   csvDirectory,
		final Iterator<File>         binaryFiles ,
		final String                 fileSuffix
	)
	{
		final String effectiveFileSuffix = "." + fileSuffix;
		
		final Predicate<File> filter = file ->
			!file.isDirectory()
			&& file.getName().endsWith(effectiveFileSuffix)
		;
		
		final long tStart = System.nanoTime();
		internalConvertFiles(storage.typeDictionary(), csvDirectory, binaryFiles, filter, null);
		final long tStop = System.nanoTime();
		System.out.println("CSV conversion completed. Elapsed Time: " + new java.text.DecimalFormat("00,000,000,000").format(tStop - tStart));

		return csvDirectory;
	}
	
	static void internalConvertFiles(
		final StorageTypeDictionary   typeDictionary    ,
		final File                    csvTargetDirectory,
		final Iterator<File>          fileProvider      ,
		final Predicate<? super File> filter            ,
		final String                  name
	)
	{
		final StorageDataConverterTypeBinaryToCsv converter = new StorageDataConverterTypeBinaryToCsv.ImplementationUTF8(
			StorageDataConverterCsvConfiguration.defaultConfiguration(),
			new StorageEntityTypeConversionFileProvider.Implementation(csvTargetDirectory, FILE_SUFFIX_CSV),
			typeDictionary,
			null,
			BUFFER_SIZE_READ,
			BUFFER_SIZE_WRITE
		);

		while(true)
		{
			final File file;
			
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
	
	static void printAction(final String name, final String action, final File file)
	{
		System.out.println((name == null ? "" : name + " ") + action + " " + file.getPath());
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
		// die Instanz muss irgendwoher aus der Anwendungslogik kommen, wo die Storage initialisiert wird.
		final EmbeddedStorageManager storage = null; // FIXME: EmbeddedStorageManager Instanz der Anwendung.
		
		// alles exportieren
		UtilStorageCsvExport.exportCsv(
			storage,
			new File("C:/StorageExportTest_2018-02-20-1600_ALL")
		);
		
		// Export-Type-Filter Beispiel: Nur alle Strings exportieren.
		UtilStorageCsvExport.exportCsv(
			storage,
			new File("C:/StorageExportTest_2018-02-20-1600_Strings"),
			t -> t.typeName().equals(String.class.getName())
		);
		
		System.exit(0);
	}
		
}
