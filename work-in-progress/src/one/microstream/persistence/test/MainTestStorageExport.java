package one.microstream.persistence.test;

import static one.microstream.X.booleans;
import static one.microstream.X.bytes;
import static one.microstream.X.chars;
import static one.microstream.X.doubles;
import static one.microstream.X.floats;
import static one.microstream.X.ints;
import static one.microstream.X.longs;
import static one.microstream.X.shorts;
import static one.microstream.X.strings;

import java.math.BigInteger;

import one.microstream.X;
import one.microstream.afs.ADirectory;
import one.microstream.afs.AFS;
import one.microstream.afs.AFile;
import one.microstream.afs.nio.NioFileSystem;
import one.microstream.collections.HashTable;
import one.microstream.collections.types.XSequence;
import one.microstream.io.XIO;
import one.microstream.storage.types.StorageConnection;


public class MainTestStorageExport extends TestStorage
{
	public static void main(final String[] args)
	{
		ROOT.set(
			new Record(true, (byte)2, (short)3, '4', 5, 6.0f, 7, 8.0d,
				booleans(true, false, true, true),
				bytes((byte)21, (byte)22, (byte)23),
				shorts((short)31, (short)32, (short)33),
				chars('A', 'B', '\n', 'C', '"'),
				ints(51, 52, 53),
				floats(6.1f, 6.2f, 6.3f),
				longs(71, 72, 73),
				doubles(8.1, 8.2, -109.9492),
				BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.valueOf(Long.MAX_VALUE)),
				strings("a", "", "\n", "a\t", "\n\n'\"'\t", "assdgdfgsdgrgbdft"),
				new long[0],
				HashTable.New(X.KeyValue(1, "A"), X.KeyValue(2, "B"), X.KeyValue(3, "C"))
			)
		);
		STORAGE.createConnection().store(ROOT);

		testExport();

//		convertCsvToBin(
//			new File("C:/Files/export/csv/one.microstream.persistence.test.Record.csv")
//			new File("C:/Files/export/csv/[Z.csv"),
//			new File("C:/Files/export/csv/[B.csv"),
//			new File("C:/Files/export/csv/[S.csv"),
//			new File("C:/Files/export/csv/[C.csv"),
//			new File("C:/Files/export/csv/[I.csv"),
//			new File("C:/Files/export/csv/[F.csv"),
//			new File("C:/Files/export/csv/[J.csv"),
//			new File("C:/Files/export/csv/[D.csv"),
//			new File("C:/Files/export/csv/java.math.BigInteger.csv"),
//			new File("C:/Files/export/csv/java.lang.String.csv"),
//			new File("C:/Files/export/csv/one.microstream.collections.HashTable.csv"),
//			new File("C:/Files/export/csv/one.microstream.persistence.types.PersistenceRoots$Default.csv")
//		);

		exit();
	}

	static void testExport()
	{
		final NioFileSystem nfs = NioFileSystem.New();
		final ADirectory directory = AFS.ensureExists(nfs.ensureDirectory(XIO.Path("C:/Files/export/bin")));
		
		final StorageConnection storageConnection = STORAGE.createConnection();
		final XSequence<AFile> exportFiles = exportTypes(
			storageConnection,
			directory,
			"dat"
		);
		convertBinToCsv(exportFiles, file -> file.identifier().endsWith(".dat"));
	}
}

class Record
{
	final boolean                   _boolean;
	final byte                      _byte   ;
	final short                     _short  ;
	final char                      _char   ;
	final int                       _int    ;
	final float                     _float  ;
	final long                      _long   ;
	final double                    _double ;
	final boolean[]                 booleans;
	final byte   []                 bytes   ;
	final short  []                 shorts  ;
	final char   []                 chars   ;
	final int    []                 ints    ;
	final float  []                 floats  ;
	final long   []                 longs   ;
	final double []                 doubles ;
	final BigInteger                big     ;
	final String[]                  strings ;
	final long   []                 empty   ;
	final HashTable<Object, Object> map     ;


	public Record(
		final boolean                   _boolean,
		final byte                      _byte   ,
		final short                     _short  ,
		final char                      _char   ,
		final int                       _int    ,
		final float                     _float  ,
		final long                      _long   ,
		final double                    _double ,
		final boolean[]                 booleans,
		final byte   []                 bytes   ,
		final short  []                 shorts  ,
		final char   []                 chars   ,
		final int    []                 ints    ,
		final float  []                 floats  ,
		final long   []                 longs   ,
		final double []                 doubles ,
		final BigInteger                big     ,
		final String []                 strings ,
		final long   []                 empty   ,
		final HashTable<Object, Object> map
	)
	{
		super();
		this._boolean = _boolean;
		this._byte    = _byte   ;
		this._short   = _short  ;
		this._char    = _char   ;
		this._int     = _int    ;
		this._float   = _float  ;
		this._long    = _long   ;
		this._double  = _double ;
		this.booleans = booleans;
		this.bytes    = bytes   ;
		this.shorts   = shorts  ;
		this.chars    = chars   ;
		this.ints     = ints    ;
		this.floats   = floats  ;
		this.longs    = longs   ;
		this.doubles  = doubles ;
		this.big      = big     ;
		this.strings  = strings ;
		this.empty    = empty   ;
		this.map      = map     ;
	}

}
