package one.microstream.storage.types;


import static one.microstream.X.notNull;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;

import one.microstream.X;
import one.microstream.collections.BulkList;
import one.microstream.collections.types.XCollection;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.exceptions.IORuntimeException;
import one.microstream.io.XIO;
import one.microstream.storage.exceptions.StorageException;
import one.microstream.storage.types.StorageTransactionsAnalysis.EntryIterator;
import one.microstream.storage.types.StorageTransactionsAnalysis.Logic;

public interface StorageTransactionsEntries
{
	public XGettingSequence<StorageTransactionsEntries.Entry> entries();
	
	
	
	public static StorageTransactionsEntries parseFile(final FileChannel fileChannel) throws IOException
	{
		final BulkList<Entry> entries = BulkList.New();
		
		StorageTransactionsAnalysis.Logic.processInputFile(
			fileChannel,
			new EntryCollector(entries)
		);
		
		return StorageTransactionsEntries.New(entries);
	}
	
	public static StorageTransactionsEntries parseFile(final Path file) throws IORuntimeException
	{
		      FileLock    lock    = null;
		final FileChannel channel = null;
		
		Throwable suppressed = null;
		try
		{
			if(!XIO.exists(file))
			{
				return StorageTransactionsEntries.New();
			}
			
			lock = ZStorageLockedFile.openLockedFileChannel(file);
			
			return parseFile(lock.channel());
		}
		catch(final IOException e)
		{
			suppressed = e;
			throw new IORuntimeException(e);
		}
		finally
		{
			XIO.unchecked.close(lock, suppressed);
			XIO.unchecked.close(channel, suppressed);
		}
	}
		
	
	public static StorageTransactionsEntries New()
	{
		return new StorageTransactionsEntries.Default(
			X.empty()
		);
	}
	
	public static StorageTransactionsEntries New(final XGettingSequence<Entry> entries)
	{
		return new StorageTransactionsEntries.Default(
			notNull(entries)
		);
	}
	
	public final class Default implements StorageTransactionsEntries
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final XGettingSequence<StorageTransactionsEntries.Entry> entries;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(final XGettingSequence<Entry> entries)
		{
			super();
			this.entries = entries;
		}


		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		@Override
		public final XGettingSequence<Entry> entries()
		{
			return this.entries;
		}
		
	}
	
	

	public interface Entry
	{
		public StorageTransactionsEntryType type();
		
		public long timestamp();

		public long fileLength();

		public long targetFileNumber();
		
		public Long sourceFileNumber();
		
		public Long specialOffset();
		
		public long lengthChange();
		
		public void setLengthChange(long lengthChange);
		
		
		public static Entry New(
			final StorageTransactionsEntryType type            ,
			final long      timestamp       ,
			final long      fileLength      ,
			final long      targetFileNumber,
			final Long      sourceFileNumber,
			final Long      specialOffset
		)
		{
			// no constraints to allow inventorizing of any transactions file, potentially inconsistent.
			return new Entry.Default(
				type            ,
				timestamp       ,
				fileLength      ,
				targetFileNumber,
				sourceFileNumber,
				specialOffset
			);
		}
		
		public final class Default implements Entry
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			private final StorageTransactionsEntryType type            ;
			private final long      timestamp       ;
			private final long      fileLength      ;
			private final long      targetFileNumber;
			private final Long      sourceFileNumber;
			private final Long      specialOffset   ;
			
			private       long      lengthChange    ;
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			Default(
				final StorageTransactionsEntryType type            ,
				final long      timestamp       ,
				final long      fileLength      ,
				final long      targetFileNumber,
				final Long      sourceFileNumber,
				final Long      specialOffset
			)
			{
				super();
				this.type             = type            ;
				this.timestamp        = timestamp       ;
				this.fileLength       = fileLength      ;
				this.targetFileNumber = targetFileNumber;
				this.sourceFileNumber = sourceFileNumber;
				this.specialOffset    = specialOffset   ;
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////

			@Override
			public final StorageTransactionsEntryType type()
			{
				return this.type;
			}

			@Override
			public final long timestamp()
			{
				return this.timestamp;
			}

			@Override
			public final long fileLength()
			{
				return this.fileLength;
			}
			
			@Override
			public final long targetFileNumber()
			{
				return this.targetFileNumber;
			}

			@Override
			public final Long sourceFileNumber()
			{
				return this.sourceFileNumber;
			}

			@Override
			public final Long specialOffset()
			{
				return this.specialOffset;
			}
			
			@Override
			public long lengthChange()
			{
				return this.lengthChange;
			}
			
			@Override
			public void setLengthChange(final long lengthChange)
			{
				this.lengthChange = lengthChange;
			}
			
			@Override
			public final String toString()
			{
				return this.type + " time=" + this.timestamp + ", fileLength=" + this.fileLength;
			}
			
		}
		
	}
	

	public final class EntryCollector implements EntryIterator
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final XCollection<Entry> entries;
		
		private long currentFileNumber = 0;
		private long currentFileLength;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		EntryCollector(final XCollection<Entry> entries)
		{
			super();
			this.entries = entries;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public boolean accept(final long address, final long availableEntryLength)
		{
			// check for and skip gaps / comments
			if(availableEntryLength < 0)
			{
				return true;
			}

			switch(Logic.getEntryType(address))
			{
				case Logic.TYPE_FILE_CREATION  : return this.parseEntryFileCreation  (address, availableEntryLength);
				case Logic.TYPE_STORE          : return this.parseEntryStore         (address, availableEntryLength);
				case Logic.TYPE_TRANSFER       : return this.parseEntryTransfer      (address, availableEntryLength);
				case Logic.TYPE_FILE_TRUNCATION: return this.parseEntryFileTruncation(address, availableEntryLength);
				case Logic.TYPE_FILE_DELETION  : return this.parseEntryFileDeletion  (address, availableEntryLength);
				default:
				{
					// (02.09.2014 TM)EXCP: proper exception
					throw new StorageException("Unknown transactions entry type: " + Logic.getEntryType(address));
				}
			}
		}
		
		private boolean parseEntryFileCreation(final long address, final long availableEntryLength)
		{
			if(availableEntryLength < Logic.LENGTH_FILE_CREATION)
			{
				return false;
			}
			
			final Entry e = Entry.New(
				StorageTransactionsEntryType.FILE_CREATION         ,
				Logic.getEntryTimestamp(address),
				Logic.getFileLength    (address),
				Logic.getFileNumber    (address),
				this.currentFileNumber          ,
				null
			);
			this.currentFileNumber = e.targetFileNumber();
			this.currentFileLength = 0;
			this.addEntry(e);
			
			return true;
			
		}
		
		private void addEntry(final Entry e)
		{
			e.setLengthChange(e.fileLength() - this.currentFileLength);
			this.currentFileLength = e.fileLength();
			
			this.entries.add(e);
		}
		
		private boolean parseEntryStore(final long address, final long availableEntryLength)
		{
			if(availableEntryLength < Logic.LENGTH_STORE)
			{
				return false;
			}
			
			final Entry e = Entry.New(
				StorageTransactionsEntryType.DATA_STORE            ,
				Logic.getEntryTimestamp(address),
				Logic.getFileLength    (address),
				this.currentFileNumber          ,
				null,
				null
			);
			this.addEntry(e);
			
//			XDebug.println("STORE " + e.targetFileNumber() + " " + e.fileLength() + " " + e.lengthChange());
			
			return true;
		}
		
		private boolean parseEntryTransfer(final long address, final long availableEntryLength)
		{
			if(availableEntryLength < Logic.LENGTH_TRANSFER)
			{
				return false;
			}
			
			final Entry e = Entry.New(
				StorageTransactionsEntryType.DATA_TRANSFER         ,
				Logic.getEntryTimestamp(address),
				Logic.getFileLength    (address),
				this.currentFileNumber          ,
				Logic.getFileNumber    (address),
				Logic.getSpecialOffset (address)
			);
			this.addEntry(e);
			
			return true;
		}
		
		private boolean parseEntryFileTruncation(final long address, final long availableEntryLength)
		{
			if(availableEntryLength < Logic.LENGTH_FILE_TRUNCATION)
			{
				return false;
			}
			
			final Entry e = Entry.New(
				StorageTransactionsEntryType.FILE_TRUNCATION       ,
				Logic.getEntryTimestamp(address),
				Logic.getFileLength    (address),
				Logic.getFileNumber    (address),
				null                            ,
				Logic.getSpecialOffset (address)
			);
			this.addEntry(e);
			
			return true;
		}
		
		private boolean parseEntryFileDeletion(final long address, final long availableEntryLength)
		{
			if(availableEntryLength < Logic.LENGTH_FILE_DELETION)
			{
				return false;
			}
			
			final Entry e = Entry.New(
				StorageTransactionsEntryType.FILE_DELETION         ,
				Logic.getEntryTimestamp(address),
				Logic.getFileLength    (address),
				Logic.getFileNumber    (address),
				null                            ,
				null
			);
			
			// no chaning of current file number or length by a delete!
			this.entries.add(e);
			
			return true;
		}
		
	}


}
