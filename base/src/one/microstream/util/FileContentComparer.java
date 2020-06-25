package one.microstream.util;

import java.nio.ByteBuffer;

import one.microstream.X;
import one.microstream.afs.AFS;
import one.microstream.afs.AFile;
import one.microstream.chars.VarString;
import one.microstream.collections.BulkList;
import one.microstream.collections.types.XGettingList;
import one.microstream.collections.types.XGettingTable;
import one.microstream.memory.XMemory;
import one.microstream.typing.KeyValue;


public class FileContentComparer
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	private static ByteBuffer readFile(final AFile file, final ByteBuffer buffer)
	{
		return AFS.apply(file, rf ->
		{
			final ByteBuffer effectiveBuffer = ensureByteBuffer(X.checkArrayRange(rf.size()), buffer);
			rf.readBytes(effectiveBuffer);
			
			return effectiveBuffer;
		});
	}
	
	private static ByteBuffer ensureByteBuffer(
		final int       requiredLength,
		final ByteBuffer buffer
	)
	{
		if(buffer == null || buffer.capacity() < requiredLength)
		{
			XMemory.deallocateDirectByteBuffer(buffer);
			final ByteBuffer newBuffer = XMemory.allocateDirectNative(requiredLength);
			return newBuffer;
		}
		
		buffer.clear();
		buffer.limit(requiredLength);
		
		return buffer;
	}
	
	public static String compareFilesAndAssembleResult(final XGettingTable<AFile, AFile> files)
	{
		final FileContentComparer fcc = FileContentComparer.New();
		fcc.compareFiles(files);
		
		return Assembler.assemble(fcc.result());
	}
	
	public static FileContentComparer New()
	{
		return new FileContentComparer();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private AFile currentSourceFile;
	private AFile currentTargetFile;
	private ByteBuffer sourceBuffer;
	private ByteBuffer targetBuffer;
	
	private long sourceBufferAddress;
	private long targetBufferAddress;
	
	private BulkList<Entry> entries;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	FileContentComparer()
	{
		super();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private void setSourceBuffer(final ByteBuffer sourceBuffer)
	{
		this.sourceBuffer = sourceBuffer;
		this.sourceBufferAddress = XMemory.getDirectByteBufferAddress(sourceBuffer);
	}
	
	private void setTargetBuffer(final ByteBuffer targetBuffer)
	{
		this.targetBuffer = targetBuffer;
		this.targetBufferAddress = XMemory.getDirectByteBufferAddress(targetBuffer);
	}
	
	public void compareFiles(final XGettingTable<AFile, AFile> files)
	{
		this.entries = BulkList.New();
		for(final KeyValue<AFile, AFile> kv : files)
		{
			this.compare(kv.key(), kv.value());
		}
	}
	
	public XGettingList<Entry> result()
	{
		return this.entries;
	}
	
	private void compare(final AFile sourceFile, final AFile targetFile)
	{
		this.currentSourceFile = sourceFile;
		this.currentTargetFile = targetFile;
		
		try
		{
			this.setSourceBuffer(readFile(sourceFile, this.sourceBuffer));
			this.setTargetBuffer(readFile(targetFile, this.targetBuffer));
			this.compareBufferedContents();
		}
		catch(final Exception e)
		{
			this.registerException(e);
		}
	}
	
	private void compareBufferedContents()
	{
		final long sourceStartAddress = this.sourceBufferAddress;
		final long targetStartAddress = this.targetBufferAddress;
		final long commonLength       = Math.min(this.sourceBuffer.position(), this.targetBuffer.position());
		
		// must compare the content even for files with different lengths
		for(int i = 0; i < commonLength; i++)
		{
			if(XMemory.get_byte(sourceStartAddress + i) != XMemory.get_byte(targetStartAddress + i))
			{
				this.registerContentMismatch(i);
				return;
			}
		}
		
		// special case check for files with different lengths
		if(this.sourceBuffer.position() != this.targetBuffer.position())
		{
			this.registerLengthMismatch();
			return;
		}
		
		// both files match
		this.registerMatch();
	}
	
	private void registerContentMismatch(final int index)
	{
		this.register(new Entry(
			this.currentSourceFile,
			this.sourceBuffer.position(),
			this.currentTargetFile,
			this.targetBuffer.position(),
			index,
			Boolean.FALSE,
			"Content Mismatch",
			null
		));
	}
	
	private void registerLengthMismatch()
	{
		this.register(new Entry(
			this.currentSourceFile,
			this.sourceBuffer.position(),
			this.currentTargetFile,
			this.targetBuffer.position(),
			Math.min(this.sourceBuffer.position(), this.targetBuffer.position()),
			Boolean.FALSE,
			"Length Mismatch",
			null
		));
	}
	
	private void registerMatch()
	{
		this.register(new Entry(
			this.currentSourceFile,
			this.sourceBuffer.position(),
			this.currentTargetFile,
			this.targetBuffer.position(),
			-1,
			Boolean.TRUE,
			"Perfect Match",
			null
		));
	}
	
	private void registerException(final Exception e)
	{
		this.register(new Entry(this.currentSourceFile, -1, this.currentTargetFile, -1, -1, null, "Exception", e));
	}
	
	private void register(final Entry entry)
	{
		this.entries.add(entry);
	}
	
	static final class Entry
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final AFile     sourceFile   ;
		final long      sourceLength ;
		final AFile     targetFile   ;
		final long      targetLength ;
		final Boolean   isMatch      ;
		final long      mismatchIndex;
		final String    message      ;
		final Exception exception    ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Entry(
			final AFile     sourceFile   ,
			final long      sourceLength ,
			final AFile     targetFile   ,
			final long      targetLength ,
			final long      mismatchIndex,
			final Boolean   isMatch      ,
			final String    message      ,
			final Exception exception
		)
		{
			super();
			this.sourceFile    = sourceFile   ;
			this.sourceLength  = sourceLength ;
			this.targetFile    = targetFile   ;
			this.targetLength  = targetLength ;
			this.isMatch       = isMatch      ;
			this.mismatchIndex = mismatchIndex;
			this.message       = message      ;
			this.exception     = exception    ;
		}


		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		public AFile sourceFile()
		{
			return this.sourceFile;
		}

		public long sourceLength()
		{
			return this.sourceLength;
		}

		public AFile targetFile()
		{
			return this.targetFile;
		}

		public long targetLength()
		{
			return this.targetLength;
		}

		public Boolean isMatch()
		{
			return this.isMatch;
		}

		public long mismatchIndex()
		{
			return this.mismatchIndex;
		}

		public String message()
		{
			return this.message;
		}

		public Exception exception()
		{
			return this.exception;
		}
		
	}
	
	
	public static final class Assembler
	{
		public static String headerSourceFile()
		{
			return "SourceFile";
		}

		public static String headerSourceLength()
		{
			return "S.FileLength";
		}

		public static String headerTargetFile()
		{
			return "TargetFile";
		}

		public static String headerTargetLength()
		{
			return "T.FileLength";
		}

		public static String headerIsMatch()
		{
			return "IsMatch";
		}

		public static String headerMismatchIndex()
		{
			return "MismatchIndex";
		}

		public static String headerMessage()
		{
			return "Message";
		}

		public static String headerException()
		{
			return "Exception";
		}
		
		
		public static String assemble(final Iterable<Entry> entries)
		{
			return assemble(VarString.New(), entries).toString();
		}
		
		public static String print(
			final Iterable<Entry> entries       ,
			final String          valueSeparator,
			final String          rowSeparator
		)
		{
			return assemble(VarString.New(), entries, valueSeparator, rowSeparator).toString();
		}
		
		public static VarString assemble(
			final VarString       vs     ,
			final Iterable<Entry> entries
		)
		{
			return assemble(vs, entries, "\t", "\n");
		}
		
		public static VarString assemble(
			final VarString       vs            ,
			final Iterable<Entry> entries       ,
			final String          valueSeparator,
			final String          rowSeparator
		)
		{
			assembleHeader(vs, valueSeparator).add(rowSeparator);
			assembleEntries(vs, entries, valueSeparator, rowSeparator);
			
			return vs;
		}
		
		public static VarString printHeader(final VarString vs)
		{
			return assembleHeader(vs, "\t");
		}
		
		public static VarString assembleHeader(final VarString vs, final String separator)
		{
			return vs
				.add(headerSourceFile()   ).add(separator)
				.add(headerSourceLength() ).add(separator)
				.add(headerTargetFile()   ).add(separator)
				.add(headerTargetLength() ).add(separator)
				.add(headerIsMatch()      ).add(separator)
				.add(headerMismatchIndex()).add(separator)
				.add(headerMessage()      ).add(separator)
				.add(headerException()    )
			;
		}
		
		public static VarString printEntries(
			final VarString       vs       ,
			final Iterable<Entry> entries
		)
		{
			return assembleEntries(vs, entries, "\t", "\n");
		}
		
		public static VarString assembleEntries(
			final VarString       vs            ,
			final Iterable<Entry> entries       ,
			final String          valueSeparator,
			final String          rowSeparator
		)
		{
			final int oldLength = vs.length();
			
			for(final Entry e : entries)
			{
				vs
				.add(e.sourceFile().toPathString()).add(valueSeparator)
				.add(e.sourceLength >= 0
					? Long.toString(e.sourceLength)
					: ""
				).add(valueSeparator)
				.add(e.targetFile().toPathString()).add(valueSeparator)
				.add(e.targetLength >= 0
					? Long.toString(e.targetLength)
					: ""
				).add(valueSeparator)
				.addMapped(e.isMatch, "Match", "Mismatch").add(valueSeparator)
				.add(e.mismatchIndex >= 0
					? Long.toString(e.mismatchIndex)
					: ""
				).add(valueSeparator)
				.add(e.message).add(valueSeparator)
				.add(e.exception != null
					? e.exception.toString()
					: ""
				)
				.add(rowSeparator);
			}
			
			if(vs.length() > oldLength)
			{
				vs.deleteLast(rowSeparator.length());
			}
			
			return vs;
		}
	}
		
}
