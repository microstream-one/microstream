package one.microstream.storage.types;

import static one.microstream.X.notNull;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import one.microstream.X;
import one.microstream.collections.BulkList;
import one.microstream.collections.types.XCollection;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.storage.types.StorageTransactionsFileAnalysis.EntryIterator;
import one.microstream.storage.types.StorageTransactionsFileAnalysis.Logic;

public interface StorageTransactionsFile
{
	public XGettingSequence<StorageTransactionsFile.Entry> entries();
	
	
	
	public static StorageTransactionsFile parseFile(final FileChannel fileChannel) throws IOException
	{
		final BulkList<Entry> entries = BulkList.New();
		
		StorageTransactionsFileAnalysis.Logic.processInputFile(
			fileChannel,
			new EntryCollector(entries)
		);
		
		return StorageTransactionsFile.New(entries);
	}
	
	public static StorageTransactionsFile parseFile(final File file)
	{
		if(!file.exists())
		{
			return StorageTransactionsFile.New();
		}
		
		      FileLock    lock    = null;
		final FileChannel channel = null;
		try
		{
			lock = StorageLockedFile.openLockedFileChannel(file);
			return parseFile(lock.channel());
		}
		catch(final IOException e)
		{
			// (12.09.2014 TM)EXCP: proper exception
			throw new RuntimeException(e);
		}
		finally
		{
			closeSilent(lock);
			closeSilent(channel);
		}
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
	
	
	public static StorageTransactionsFile New()
	{
		return new StorageTransactionsFile.Implementation(
			X.empty()
		);
	}
	
	public static StorageTransactionsFile New(final XGettingSequence<Entry> entries)
	{
		return new StorageTransactionsFile.Implementation(
			notNull(entries)
		);
	}
	
	public final class Implementation implements StorageTransactionsFile
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final XGettingSequence<StorageTransactionsFile.Entry> entries;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(final XGettingSequence<Entry> entries)
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
	
	

	public enum EntryType
	{
		FILE_CREATION  ("CREATION"  , Logic.TYPE_FILE_CREATION  , Logic.LENGTH_FILE_CREATION  ),
		DATA_STORE     ("STORE"     , Logic.TYPE_STORE          , Logic.LENGTH_STORE          ),
		DATA_TRANSFER  ("TRANSFER"  , Logic.TYPE_TRANSFER       , Logic.LENGTH_TRANSFER       ),
		FILE_TRUNCATION("TRUNCATION", Logic.TYPE_FILE_TRUNCATION, Logic.LENGTH_FILE_TRUNCATION),
		FILE_DELETION  ("DELETION"  , Logic.TYPE_FILE_DELETION  , Logic.LENGTH_FILE_DELETION  );
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final String typeName;
		private final byte   code    ;
		private final int    length  ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		private EntryType(final String typeName, final byte code, final int length)
		{
			this.typeName = typeName;
			this.code     = code    ;
			this.length   = length  ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		public byte code()
		{
			return this.code;
		}
		
		public String typeName()
		{
			return this.typeName;
		}
		
		public int length()
		{
			return this.length;
		}
		
		@Override
		public String toString()
		{
			return this.typeName + "(" + this.code + "," + this.length + ")";
		}
		
		public static EntryType fromCode(final byte code)
		{
			switch(code)
			{
				case Logic.TYPE_FILE_CREATION  : return EntryType.FILE_CREATION  ;
				case Logic.TYPE_STORE          : return EntryType.DATA_STORE     ;
				case Logic.TYPE_TRANSFER       : return EntryType.DATA_TRANSFER  ;
				case Logic.TYPE_FILE_TRUNCATION: return EntryType.FILE_TRUNCATION;
				case Logic.TYPE_FILE_DELETION  : return EntryType.FILE_DELETION  ;
				default:
				{
					// (02.09.2014)EXCP: proper exception
					throw new RuntimeException("Unknown transactions entry type: " + code);
				}
			}
		}
		
	}
	
	
	public interface Entry
	{
		public EntryType type();
		
		public long timestamp();

		public long fileLength();
		
		public Long fileNumber();
		
		public Long specialOffset();
		
		
		public static Entry New(
			final EntryType type         ,
			final long      timestamp    ,
			final long      fileLength   ,
			final Long      fileNumber   ,
			final Long      specialOffset
		)
		{
			// no constraints to allow inventorizing of any transactions file, potentially inconsistent.
			return new Entry.Implementation(
				type         ,
				timestamp    ,
				fileLength   ,
				fileNumber   ,
				specialOffset
			);
		}
		
		public final class Implementation implements Entry
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			private final EntryType type         ;
			private final long      timestamp    ;
			private final long      fileLength   ;
			private final Long      fileNumber   ;
			private final Long      specialOffset;
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			Implementation(
				final EntryType type         ,
				final long      timestamp    ,
				final long      fileLength   ,
				final Long      fileNumber   ,
				final Long      specialOffset
			)
			{
				super();
				this.type          = type         ;
				this.timestamp     = timestamp    ;
				this.fileLength    = fileLength   ;
				this.fileNumber    = fileNumber   ;
				this.specialOffset = specialOffset;
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////

			@Override
			public final EntryType type()
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
			public final Long fileNumber()
			{
				return this.fileNumber;
			}

			@Override
			public final Long specialOffset()
			{
				return this.specialOffset;
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
					// (02.09.2014)EXCP: proper exception
					throw new RuntimeException("Unknown transactions entry type: " + Logic.getEntryType(address));
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
				EntryType.FILE_CREATION         ,
				Logic.getEntryTimestamp(address),
				Logic.getFileLength    (address),
				Logic.getFileNumber    (address),
				null
			);
			this.entries.add(e);
			
			return true;
			
		}
		
		private boolean parseEntryStore(final long address, final long availableEntryLength)
		{
			if(availableEntryLength < Logic.LENGTH_STORE)
			{
				return false;
			}
			
			final Entry e = Entry.New(
				EntryType.DATA_STORE            ,
				Logic.getEntryTimestamp(address),
				Logic.getFileLength    (address),
				null,
				null
			);
			this.entries.add(e);
			
			return true;
		}
		
		private boolean parseEntryTransfer(final long address, final long availableEntryLength)
		{
			if(availableEntryLength < Logic.LENGTH_TRANSFER)
			{
				return false;
			}
			
			final Entry e = Entry.New(
				EntryType.DATA_TRANSFER         ,
				Logic.getEntryTimestamp(address),
				Logic.getFileLength    (address),
				Logic.getFileNumber    (address),
				Logic.getSpecialOffset (address)
			);
			this.entries.add(e);
			
			return true;
		}
		
		private boolean parseEntryFileTruncation(final long address, final long availableEntryLength)
		{
			if(availableEntryLength < Logic.LENGTH_FILE_TRUNCATION)
			{
				return false;
			}
			
			final Entry e = Entry.New(
				EntryType.FILE_TRUNCATION       ,
				Logic.getEntryTimestamp(address),
				Logic.getFileLength    (address),
				Logic.getFileNumber    (address),
				Logic.getSpecialOffset (address)
			);
			this.entries.add(e);
			
			return true;
		}
		
		private boolean parseEntryFileDeletion(final long address, final long availableEntryLength)
		{
			if(availableEntryLength < Logic.LENGTH_FILE_DELETION)
			{
				return false;
			}
			
			final Entry e = Entry.New(
				EntryType.FILE_DELETION         ,
				Logic.getEntryTimestamp(address),
				Logic.getFileLength    (address),
				Logic.getFileNumber    (address),
				null
			);
			this.entries.add(e);
			
			return true;
		}
		
	}


}
