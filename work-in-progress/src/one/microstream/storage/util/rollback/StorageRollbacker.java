package one.microstream.storage.util.rollback;

import static one.microstream.X.notNull;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import one.microstream.X;
import one.microstream.memory.XMemory;
import one.microstream.chars.VarString;
import one.microstream.collections.BulkList;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.XSort;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XGettingTable;
import one.microstream.io.XFiles;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.Persistence;
import one.microstream.storage.types.StorageTransactionsFile;
import one.microstream.typing.KeyValue;

class StorageRollbacker
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	final long                          lowestFileNumber;
	final EntityDataHeaderEvaluator     headerEvaluator ;
	final EqHashTable<Long, SourceFile> sourceFiles     ;
	final File                          recDirectory    ;
	final String                        recFilePrefix   ;
	final String                        storeFilePrefix ;
	
	final EqHashTable<Long, StoreFile> storeFiles = EqHashTable.New();
	final EqHashTable<Long, RecFile>   recFiles   = EqHashTable.New();
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	StorageRollbacker(
		final long                      lowestFileNumber,
		final XGettingTable<Long, File> sourceFiles     ,
		final File                      recDirectory    ,
		final String                    recFilePrefix   ,
		final String                    storeFilePrefix ,
		final EntityDataHeaderEvaluator headerEvaluator
	)
	{
		super();
		this.headerEvaluator  = headerEvaluator ;
		this.sourceFiles      = this.setupSourceFiles(sourceFiles);
		this.recDirectory     = recDirectory    ;
		this.recFilePrefix    = recFilePrefix   ;
		this.storeFilePrefix  = storeFilePrefix ;
		this.lowestFileNumber = lowestFileNumber;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private final EqHashTable<Long, SourceFile> setupSourceFiles(
		final XGettingTable<Long, File> inputSourceFiles
	)
	{
		final EqHashTable<Long, SourceFile> sourceFiles = EqHashTable.New();
		
		for(final KeyValue<Long, File> e : inputSourceFiles)
		{
			sourceFiles.add(e.key(), new SourceFile.Default(e.key(), e.value()));
		}
		
		return sourceFiles;
	}

	public void rollbackTransfers(final StorageTransactionsFile tf)
		 throws Exception
	{
		final long quickNDirtyCertainlyLastFile = this.lowestFileNumber - 1;
		
		final XGettingSequence<StorageTransactionsFile.Entry> reversed = tf.entries().toReversed();
		for(final StorageTransactionsFile.Entry e : reversed)
		{
			if(e.targetFileNumber() == quickNDirtyCertainlyLastFile)
			{
				break;
			}
			this.handleTransactionsEntry(e);
		}
	
		this.processRecEntries();
		this.processStoreEntries();
		this.cleanUpStores();
	}
	
	public void cleanUpDirect() throws Exception
	{
		final File cleanedFile = this.createCleanUpFile();
		final FileChannel channel = openChannel(cleanedFile);
		
		for(final SourceFile file : this.sourceFiles.values())
		{
			this.cleanUp(file, channel);
		}
	}
	
	public void recoverStringsAndPrint() throws Exception
	{
		final EqHashTable<Long, String> strings = this.recoverStrings();
		
		final VarString vs = VarString.New();
		
		for(final KeyValue<Long, String> e : strings)
		{
			vs.add("[[["+e.key()+"]]]:").lf();
			vs.add(e.value()).lf();
			vs.lf();
		}
		
		XFiles.writeStringToFile(new File(this.recDirectory, "lcm_prod_Strings.txt"), vs.toString());
	}
	
	public EqHashTable<Long, String> recoverStrings() throws Exception
	{
		final EqHashTable<Long, String> strings = EqHashTable.New();
		
		for(final SourceFile file : this.sourceFiles.values())
		{
			this.recoverStrings(strings, file);
		}
		
		strings.keys().sort(XSort::compare);
		return strings;
	}
	
	public static final class StringRecognizer
	{
		static final long
			LENGTH_LOWER_VALUE   = Binary.entityHeaderLength()       ,
			LENGTH_UPPER_BOUND   = 100_000                           ,
			TYPEID_STRING        = 30                                ,
			OBJECTID_LOWER_VALUE = Persistence.defaultStartObjectId(),
			OBJECTID_UPPER_BOUND = 1000000000001000000L
		;
		
		public boolean isString(final long entityStartAddress, final long availableDataLength)
		{
			if(Binary.entityHeaderLength() > availableDataLength)
			{
				return false;
			}
			final long typeId = Binary.getEntityTypeIdRawValue(entityStartAddress);
			if(typeId != TYPEID_STRING)
			{
				return false;
			}
			
			final long length = Binary.getEntityLengthRawValue(entityStartAddress);
			if(!isValid(LENGTH_LOWER_VALUE, LENGTH_UPPER_BOUND, length))
			{
				return false;
			}
			
			final long objectId = Binary.getEntityObjectIdRawValue(entityStartAddress);
			if(!isValid(OBJECTID_LOWER_VALUE, OBJECTID_UPPER_BOUND, objectId))
			{
				return false;
			}

			if(length > availableDataLength)
			{
				return false;
			}
			
			return true;
		}
	}
	
	public void recoverStrings(
		final EqHashTable<Long, String> strings  ,
		final SourceFile                storeFile
	)
		throws Exception
	{
		final ByteBuffer    dbb = readFile(storeFile.fileChannel());
		final long startAddress = XMemory.getDirectByteBufferAddress(dbb);
		final long boundAddress = startAddress + dbb.position();
		
		final StringRecognizer validator = new StringRecognizer();
				
		long a = startAddress;
		while(a < boundAddress)
		{
			if(validator.isString(a, boundAddress - a))
			{
				a = this.collectString(strings, a);
			}
			else
			{
				a++;
			}
		}
	}
	
	private static final long STRING_HEADER_LENGTH = Binary.entityHeaderLength() + 16; // 16 = list header length
	
	private long collectString(
		final EqHashTable<Long, String> strings,
		final long      entityStartAddress
	)
		throws Exception
	{

		final long length   = Binary.getEntityLengthRawValue(entityStartAddress)  ;
		final long objectId = Binary.getEntityObjectIdRawValue(entityStartAddress);
		
		final int stringContentLength = X.checkArrayRange(length - STRING_HEADER_LENGTH);
		final char[] buffer = new char[stringContentLength / 2];
		XMemory.copyRangeToArray(entityStartAddress + STRING_HEADER_LENGTH, buffer);
		final String string = new String(buffer);
		
		strings.add(objectId, string);
		
		return entityStartAddress + length;
	}
	
	private void processRecEntries() throws Exception
	{
		for(final RecFile r : this.recFiles.values())
		{
//			XDebug.println("Filling RecFile " + r.number);
			System.out.println("Filling RecFile " + r.number);
			for(final RecEntry e : r.recEntries)
			{
//				XDebug.println(
				System.out.println(
					"Copying"
					+ " from source " + e.sourceFile.number() + "@" + e.sourceOffset + "[" + e.length + "]"
					+ " to RecFile "  + r.number()            + "@" + e.targetOffset
				);
				r.fileChannel.position(e.targetOffset);
				e.sourceFile.fileChannel().position(e.sourceOffset);
				r.fileChannel.transferFrom(e.sourceFile.fileChannel(), e.targetOffset, e.length);
				r.fileChannel.force(false);
			}
		}
	}
	
	private void processStoreEntries() throws Exception
	{
		for(final StoreFile s : this.storeFiles.values())
		{
//			XDebug.println("Filling StoreFile " + s.number);
			System.out.println("Filling StoreFile " + s.number);
			for(final StoreEntry e : s.storeEntries)
			{
//				XDebug.println(
				System.out.println(
					"Copying"
					+ " from source "  + e.sourceFile.number() + "@" + e.sourceOffset + "[" + e.length + "]"
					+ " to StoreFile " + s.number()            + "@" + + s.fileChannel.size()
				);
				e.sourceFile.fileChannel().position(e.sourceOffset);
				s.fileChannel.transferFrom(e.sourceFile.fileChannel(), s.fileChannel.size(), e.length);
				s.fileChannel.force(false);
			}
		}
	}
	
	private void cleanUpStores() throws Exception
	{
		final File cleanedFile = this.createCleanUpFile();
		final FileChannel channel = openChannel(cleanedFile);
		
		for(final SourceFile s : this.storeFiles.values())
		{
			this.cleanUp(s, channel);
		}
	}
	
	private File createCleanUpFile()
	{
		return new File(
			XFiles.ensureDirectory(new File(this.recDirectory, "cleaned")),
			"channel_0_" + (this.lowestFileNumber + 1) + ".dat"
		);
	}
	
	private void cleanUp(final SourceFile storeFile, final FileChannel channel) throws Exception
	{
		final ByteBuffer    dbb = readFile(storeFile.fileChannel());
		final long startAddress = XMemory.getDirectByteBufferAddress(dbb);
		final long boundAddress = startAddress + dbb.position();
		
		final EntityDataHeaderEvaluator validator = this.headerEvaluator;
		
		long currentValidEntityStartAddress = startAddress;
		long currentValidEntityBoundAddress = currentValidEntityStartAddress;
		
		long a = startAddress;
		outer:
		while(a < boundAddress)
		{
			long s = a;
			while(XMemory.get_byte(s) == 0)
			{
				if(++s == boundAddress)
				{
					break outer;
				}
			}
			if(s != a)
			{
				this.flushValidEntities(channel, currentValidEntityStartAddress, currentValidEntityBoundAddress);
				System.out.println("Skipping zeroes  in store file " + storeFile.number()
				+ " ["+(a - startAddress)+";"+(s - startAddress)+"[("+(s-a)+")");
				a = s;
				currentValidEntityBoundAddress = currentValidEntityStartAddress = s;
			}
			
			long v = a;
			while(!validator.isValidHeader(v, boundAddress - v))
			{
				if(++v == boundAddress)
				{
					break outer;
				}
			}
			if(v != a)
			{
				this.flushValidEntities(channel, currentValidEntityStartAddress, currentValidEntityBoundAddress);
				currentValidEntityBoundAddress = currentValidEntityStartAddress = 0;
				System.out.println("Skipping garbage in store file " + storeFile.number()
				+ " ["+(a - startAddress)+";"+(v - startAddress)+"[("+(v-a)+")"
			);
				this.writeGargabe(storeFile, startAddress, v, v - a);
				a = v;
				currentValidEntityBoundAddress = currentValidEntityStartAddress = v;
			}
			
			final long length   = Binary.getEntityLengthRawValue(a)  ;
			final long typeId   = Binary.getEntityTypeIdRawValue(a)  ;
			final long objectId = Binary.getEntityObjectIdRawValue(a);
			
			System.out.println(
				"Recovering entity in store file " + storeFile.number() + " @" + a
				+ ": [" + length + "][" + typeId + "][" + objectId + "]"
				+ " (" + (channel.size() + currentValidEntityBoundAddress - currentValidEntityStartAddress) + ")"
			);
			currentValidEntityBoundAddress += length;
			a += length;
		}
		
		this.flushValidEntities(channel, currentValidEntityStartAddress, currentValidEntityBoundAddress);
	}
	
	private void flushValidEntities(
		final FileChannel channel,
		final long        address,
		final long        boundAddress
	)
		throws Exception
	{
		if(address == boundAddress)
		{
			return;
		}
		writeBytes(address, boundAddress - address, channel);
	}
	
	private void writeGargabe(
		final SourceFile storeFile  ,
		final long       addressBase,
		final long       address    ,
		final long       length
	)
		throws Exception
	{
		final File partFile = new File(
			XFiles.ensureDirectory(new File(this.recDirectory, "garbage")),
			"Garbage_" + storeFile.number() + "_@" + (address - addressBase) + "[" + length + "]"
		);
		writeBytes(address, length, partFile);
	}
	
	private static void writeBytes(final long address, final long length, final File file) throws Exception
	{
		writeBytes(address, length, openChannel(file));
	}
	
	private static void writeBytes(
		final long        address,
		final long        length ,
		final FileChannel channel
	)
		throws Exception
	{
//		XDebug.println("Writing @" + address + "[" + length + "]");
		
		final ByteBuffer dbb = XMemory.allocateDirectNative(length);
		XMemory.copyRange(address, XMemory.getDirectByteBufferAddress(dbb), length);
		
		while(dbb.hasRemaining())
		{
			channel.write(dbb);
		}
		/* Intentionally no close since the cleanedStores file is kept open
		 * All channels are closed when the programm terminates, anyway.
		 * This is (currently!) only a oneshot-"script", not an application.
		 */
	}
	
	public static void closeSilent(final AutoCloseable closable)
	{
		if(closable == null)
		{
			return;
		}
		try
		{
			closable.close();
		}
		catch(final Exception t)
		{
			// sshhh, silence!
		}
	}
	
	private static ByteBuffer readFile(final FileChannel channel) throws Exception
	{
		final ByteBuffer bb = XMemory.allocateDirectNative(channel.size());
		
		while(bb.hasRemaining())
		{
			channel.read(bb);
		}
		
		return bb;
	}
	
	private boolean handleTransactionsEntry(final StorageTransactionsFile.Entry e) throws Exception
	{
		switch(e.type())
		{
			case FILE_CREATION  : return this.handleTransactionsEntryFileCreation(e);
			case DATA_STORE     : return this.handleTransactionsEntryDataStore(e);
			case DATA_TRANSFER  : return this.handleTransactionsDataTransfer(e);
			case FILE_TRUNCATION: return this.handleTransactionsEntryFileTruncation(e);
			case FILE_DELETION  : return this.handleTransactionsEntryFileDeletion(e);
			default: throw new Error(
				"Unknown " + StorageTransactionsFile.Entry.class.getSimpleName() + ": " + e.type()
			);
		}
	}
	
	private boolean handleTransactionsEntryFileCreation(final StorageTransactionsFile.Entry e)
	{
		// file creations are not relevant for rollback
		return true;
	}
	
	private boolean handleTransactionsEntryDataStore(final StorageTransactionsFile.Entry e)
	{
		final StoreFile  storeFile  = this.ensureStoreFile(e.targetFileNumber());
		final SourceFile sourceFile = this.sourceFiles.get(e.targetFileNumber());
		notNull(sourceFile);
		storeFile.storeEntries.add(
			new StoreEntry(e, sourceFile, e.fileLength() - e.lengthChange(), e.lengthChange())
		);
		return true;
	}
	
	private boolean handleTransactionsDataTransfer(final StorageTransactionsFile.Entry e)
	{
		// target and source must be switched for recovery files. It's a rollback!
		final RecFile    recFile    = this.ensureRecFile(e.sourceFileNumber());
		final SourceFile sourceFile = this.sourceFiles.get(e.targetFileNumber());
		notNull(sourceFile);
		
		// source offset and target offset have to be switched, too.
		recFile.recEntries.add(
			new RecEntry(
				e,
				sourceFile,
				e.fileLength() - e.lengthChange(),
				e.lengthChange(),
				e.specialOffset()
			)
		);
		return true;
	}
	
	private boolean handleTransactionsEntryFileTruncation(final StorageTransactionsFile.Entry e)
	{
		// not required in the current case
		throw new one.microstream.meta.NotImplementedYetError();
	}
	
	private boolean handleTransactionsEntryFileDeletion(final StorageTransactionsFile.Entry e) throws Exception
	{
//		XDebug.println("Creating RecFile " + e.targetFileNumber() + " with length " + e.fileLength());
		System.out.println("Creating RecFile " + e.targetFileNumber() + " with length " + e.fileLength());
		final RecFile r = this.ensureRecFile(e.targetFileNumber());
		r.fill(e.fileLength());
		return true;
	}
	
	private RecFile ensureRecFile(final Long fileNumber)
	{
		notNull(fileNumber);
		return this.recFiles.ensure(fileNumber, this::createRecFile);
	}
	
	private StoreFile ensureStoreFile(final Long fileNumber)
	{
		notNull(fileNumber);
		return this.storeFiles.ensure(fileNumber, this::createStoreFile);
	}

	private RecFile createRecFile(final Long fileNumber)
	{
		final File file = new File(
			XFiles.ensureDirectory(new File(this.recDirectory, "rollback")),
			this.recFilePrefix + fileNumber + ".dat"
		);
		final RecFile rf = new RecFile(fileNumber, file);
		this.sourceFiles.add(fileNumber, rf);
		
		return rf;
	}

	private StoreFile createStoreFile(final Long fileNumber)
	{
		final File file = new File(
			XFiles.ensureDirectory(new File(this.recDirectory, "stores")),
			this.storeFilePrefix + fileNumber + ".dat"
		);
		return new StoreFile(fileNumber, file);
	}
	

	@SuppressWarnings("resource")
	static FileChannel openChannel(final File file)
	{
		try
		{
			return new RandomAccessFile(file, "rw").getChannel();
		}
		catch(final Exception e)
		{
			throw new Error(e);
		}
	}
	
	
	static class RecFile extends AbstractFile implements SourceFile
	{
		final BulkList<RecEntry> recEntries = BulkList.New();
		
		RecFile(final Long number, final File file)
		{
			super(number, file);
		}
		
		RecFile fill(final long size) throws IOException
		{
			final ByteBuffer bb = ByteBuffer.allocate(X.checkArrayRange(size));
			while(bb.hasRemaining())
			{
				bb.put((byte)0);
			}
			bb.flip();
			
			while(bb.hasRemaining())
			{
				this.fileChannel.write(bb);
			}
			
			return this;
		}
		
	}
	
	static class RecEntry
	{
		StorageTransactionsFile.Entry tfe         ;
		SourceFile                    sourceFile  ;
		long                          sourceOffset;
		long                          length      ;
		long                          targetOffset;
		
		RecEntry(
			final StorageTransactionsFile.Entry tfe         ,
			final SourceFile                    sourceFile  ,
			final long                          sourceOffset,
			final long                          length      ,
			final long                          targetOffset
		)
		{
			super();
			this.tfe          = tfe         ;
			this.sourceFile   = sourceFile  ;
			this.sourceOffset = sourceOffset;
			this.length       = length      ;
			this.targetOffset = targetOffset;
		}
		
	}
	
	static class StoreEntry
	{
		StorageTransactionsFile.Entry tfe         ;
		SourceFile                    sourceFile  ;
		long                          sourceOffset;
		long                          length      ;
		
		StoreEntry(
			final StorageTransactionsFile.Entry tfe         ,
			final SourceFile                    sourceFile  ,
			final long                          sourceOffset,
			final long                          length
		)
		{
			super();
			this.tfe          = tfe         ;
			this.sourceFile   = sourceFile  ;
			this.sourceOffset = sourceOffset;
			this.length       = length      ;
		}
		
	}
	
	static class StoreFile extends AbstractFile implements SourceFile
	{
		final BulkList<StoreEntry> storeEntries = BulkList.New();
		
		StoreFile(final Long number, final File file)
		{
			super(number, file);
		}
				
	}

	static abstract class AbstractFile
	{
		final Long        number    ;
		final File        file      ;
		final FileChannel fileChannel;

		AbstractFile(final Long number, final File file)
		{
			super();
			this.number      = number           ;
			this.file        = file             ;
			this.fileChannel = openChannel(file);
		}
		
		public final Long number()
		{
			return this.number;
		}
		
		public final File file()
		{
			return this.file;
		}
		
		public final FileChannel fileChannel()
		{
			return this.fileChannel;
		}
	}
	
	interface SourceFile
	{
		public Long number();
		
		public File file();
		
		public FileChannel fileChannel();
		
		
		public final class Default extends AbstractFile implements SourceFile
		{
			Default(final Long number, final File file)
			{
				super(number, file);
			}
			
			// no method implementations. It's magic! :D
		}
	}
	
	static final boolean isValid(final long lowerValue, final long upperBound, final long value)
	{
		if(value < lowerValue)
		{
			return false;
		}
		if(value >= upperBound)
		{
			return false;
		}
		
		return true;
	}
	
	public static final class EntityDataHeaderEvaluator
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final long
			lengthLowerValue  ,
			lengthUpperBound  ,
			typeIdLowerValue  ,
			typeIdUpperBound  ,
			objectIdLowerValue,
			objectIdUpperBound
		;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public EntityDataHeaderEvaluator(
			final long lengthLowerValue  ,
			final long lengthUpperBound  ,
			final long typeIdLowerValue  ,
			final long typeIdUpperBound  ,
			final long objectIdLowerValue,
			final long objectIdUpperBound
		)
		{
			super();
			this.lengthLowerValue   = lengthLowerValue  ;
			this.lengthUpperBound   = lengthUpperBound  ;
			this.typeIdLowerValue   = typeIdLowerValue  ;
			this.typeIdUpperBound   = typeIdUpperBound  ;
			this.objectIdLowerValue = objectIdLowerValue;
			this.objectIdUpperBound = objectIdUpperBound;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		

		
		public boolean isValidHeader(final long entityStartAddress, final long availableDataLength)
		{
			if(Binary.entityHeaderLength() > availableDataLength)
			{
				return false;
			}
			final long length   = Binary.getEntityLengthRawValue(entityStartAddress)  ;
			final long typeId   = Binary.getEntityTypeIdRawValue(entityStartAddress)  ;
			final long objectId = Binary.getEntityObjectIdRawValue(entityStartAddress);
			
			if(!this.isValidHeader(length, typeId, objectId))
			{
				return false;
			}

			if(length > availableDataLength)
			{
				return false;
			}
			
			return true;
		}
		
		public boolean isValidHeader(final long length, final long typeId, final long objectId)
		{
			if(!isValid(this.lengthLowerValue, this.lengthUpperBound, length))
			{
				return false;
			}
			if(!isValid(this.typeIdLowerValue, this.typeIdUpperBound, typeId))
			{
				return false;
			}
			if(!isValid(this.objectIdLowerValue, this.objectIdUpperBound, objectId))
			{
				return false;
			}
			
			return true;
		}
		
	}
	
}
