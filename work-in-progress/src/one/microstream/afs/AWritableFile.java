package one.microstream.afs;

import java.nio.ByteBuffer;

public interface AWritableFile extends AReadableFile
{
	public void openWriting();
	
	public boolean isOpenWriting();
	
	// ONLY the writing IO-Aspect, not the AFS-management-level aspect. Reading aspect remains open.
	public void closeWriting();
	
	public boolean isClosedWriting();

	// implicitely #closeWriting PLUS the AFS-management-level WRITING aspect. BOTH reading aspects remain!
	public boolean releaseWriting();
	
	// (29.04.2020 TM)FIXME: priv#49: reimplement to call #releaseWriting implicitely
	@Override
	public boolean release();
	
	
	
	public long write(Iterable<? extends ByteBuffer> sources);
	
	
	
	public static <W extends AWritableFile> Entry<W> Entry()
	{
		return new Entry.Default<>();
	}
	
	public interface Entry<W extends AWritableFile>
	{
		public Object writer();
		
		public W file();

		public Object setWriter(Object writer);
		
		public W setFile(W file);
		
		static final class Default<W extends AWritableFile> implements AWritableFile.Entry<W>
		{
			private Object writer;
			private W      file  ;
			
			Default()
			{
				super();
			}

			@Override
			public final Object writer()
			{
				return this.writer;
			}

			@Override
			public final W file()
			{
				return this.file;
			}

			@Override
			public final Object setWriter(final Object writer)
			{
				final Object oldWriter = this.writer;
				this.writer = writer;
				
				return oldWriter;
			}

			@Override
			public final W setFile(final W file)
			{
				final W oldFile = this.file;
				this.file = file;
				
				return oldFile;
			}
			
		}
		
	}
			
}
