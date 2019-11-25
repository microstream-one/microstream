package one.microstream.storage.util;

import java.io.File;
import java.nio.file.Path;

import one.microstream.X;
import one.microstream.chars.VarString;
import one.microstream.io.XPaths;
import one.microstream.storage.types.StorageTransactionsFileAnalysis;
import one.microstream.util.cql.CQL;


/**
 * Inoffizielle Helfer-Util-Klasse, um den Inhalt von OGS Transaction Files auszulesen.
 * <br>
 * <br>
 * Siehe<br>
 * {@link UtilStoragePrintTransactionFiles#printTransactionsFile(File)}<br>
 * {@link UtilStoragePrintTransactionFiles#printTransactionsFiles(File...)}<br>
 * {@link UtilStoragePrintTransactionFiles#printTransactionsFiles(String...)}<br>
 * 
 * @author TM
 */
public class UtilStoragePrintTransactionFiles
{

	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	/**
	 * Convenience-Methode, die die übergebenen Strings als Dateipfade interpretiert und an
	 * {@link UtilStoragePrintTransactionFiles#printTransactionsFiles(File...)} übergibt.
	 * 
	 * @param filePaths Die Dateipfade zu den Transactions-Dateien, die ausgelesen werden sollen.
	 */
	public static void printTransactionsFiles(final String... filePaths)
	{
		printTransactionsFiles(
			CQL
			.from(X.List(filePaths))
			.project(XPaths::Path)
			.execute()
			.toArray(Path.class)
		);
	}
		
	/**
	 * Ruft für jedes übergebene {@link File} {@link UtilStoragePrintTransactionFiles#printTransactionsFile(File)} auf.
	 * Verzeichnisse werden ignoriert.
	 * 
	 * @param files die auszulesenden Transactions Files.
	 */
	public static void printTransactionsFiles(final Path... files)
	{
		for(final Path file : files)
		{
			if(XPaths.isDirectoryUnchecked(file))
			{
				continue;
			}
			
			printTransactionsFile(file);
		}
	}
	
	/**
	 * Liest den Inhalt der übergebenen Datei als OGS Transactions Datei und konvertiert diesen in eine \t-getrennt
	 * tabellarische String-Form.
	 * 
	 * @param file Die auszulesende Transactions Datei.
	 */
	public static void printTransactionsFile(final Path file)
	{
		final VarString vs = VarString.New(file.toString()).lf();
		StorageTransactionsFileAnalysis.EntryAssembler.assembleHeader(vs, "\t").lf();
		final VarString s = StorageTransactionsFileAnalysis.Logic.parseFile(file, vs);
		System.out.println(s);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// test //
	/////////
	
	public static void main(final String[] args)
	{
		// Verwendungsbeispiel. Pfad je nach Anwendung, natürlich.
		printTransactionsFiles(
			"C:/localTestApplication/storage/channel_0/transactions_0.sft",
			"C:/localTestApplication/storage/channel_1/transactions_1.sft",
			"C:/localTestApplication/storage/channel_2/transactions_2.sft",
			"C:/localTestApplication/storage/channel_3/transactions_3.sft"
		);
	}
	
}
