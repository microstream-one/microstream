package one.microstream.test.corp.logic;

import static one.microstream.X.notNull;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import one.microstream.X;
import one.microstream.collections.BulkList;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XGettingTable;
import one.microstream.storage.types.StorageTransactionsFile;
import one.microstream.typing.KeyValue;

class StorageRollbacker
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	final EqHashTable<Long, SourceFile> sourceFiles    ;
	final File                          recDirectory   ;
	final File                          storeDirectory ;
	final String                        recFilePrefix  ;
	final String                        storeFilePrefix;
	
	final EqHashTable<Long, StoreFile> storeFiles = EqHashTable.New();
	final EqHashTable<Long, RecFile>   recFiles   = EqHashTable.New();
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	StorageRollbacker(
		final XGettingTable<Long, File> sourceFiles    ,
		final File                      recDirectory   ,
		final File                      storeDirectory ,
		final String                    recFilePrefix  ,
		final String                    storeFilePrefix
	)
	{
		super();
		this.sourceFiles     = this.setupSourceFiles(sourceFiles);
		this.recDirectory    = recDirectory   ;
		this.storeDirectory  = storeDirectory ;
		this.recFilePrefix   = recFilePrefix  ;
		this.storeFilePrefix = storeFilePrefix;
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
			sourceFiles.add(e.key(), new SourceFile.Implementation(e.key(), e.value()));
		}
		
		return sourceFiles;
	}

	public void rollbackTransfers(
		final StorageTransactionsFile tf            ,
		final long                    lastFileNumber,
		final long                    lastFileLength
	)
		 throws Exception
	{
		final long quickNDirtyCertainlyLastFile = lastFileNumber - 1;
		
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
	}
	
	private void processRecEntries() throws Exception
	{
		for(final RecFile r : this.recFiles.values())
		{
			// (14.03.2019 TM)FIXME: SYSO
//			XDebug.println("Filling RecFile " + r.number);
			System.out.println("Filling RecFile " + r.number);
			for(final RecEntry e : r.recEntries)
			{
				// (14.03.2019 TM)FIXME: SYSO
//				XDebug.println(
				System.out.println(
					"Copying to RecFile " + r.number()
					+ " from source " + e.sourceFile.number() + "@" + e.sourceOffset + "[" + e.length + "]"
					+ " to " + e.targetOffset
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
			// (14.03.2019 TM)FIXME: SYSO
//			XDebug.println("Filling StoreFile " + s.number);
			System.out.println("Filling StoreFile " + s.number);
			for(final StoreEntry e : s.storeEntries)
			{
				// (14.03.2019 TM)FIXME: SYSO
//				XDebug.println(
				System.out.println(
					"Copying to StoreFile " + s.number()
					+ " from source " + e.sourceFile.number() + "@" + e.sourceOffset + "[" + e.length + "]"
					+ " to " + s.fileChannel.size()
				);
				e.sourceFile.fileChannel().position(e.sourceOffset);
				s.fileChannel.transferFrom(e.sourceFile.fileChannel(), s.fileChannel.size(), e.length);
				s.fileChannel.force(false);
			}
		}
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
		// (14.03.2019 TM)FIXME: SYSO
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
		final File file = new File(this.recDirectory, this.recFilePrefix + fileNumber + ".dat");
		final RecFile rf = new RecFile(fileNumber, file);
		this.sourceFiles.add(fileNumber, rf);
		
		return rf;
	}

	private StoreFile createStoreFile(final Long fileNumber)
	{
		final File file = new File(this.storeDirectory, this.storeFilePrefix + fileNumber + ".dat");
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
	
	static class StoreFile extends AbstractFile
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
		
		
		public final class Implementation extends AbstractFile implements SourceFile
		{
			Implementation(final Long number, final File file)
			{
				super(number, file);
			}
			
			// no method implementations. It's magic! :D
		}
	}
		
	
}
