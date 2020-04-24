package one.microstream.afs;

import java.nio.ByteBuffer;

public interface AWritableFile extends AReadableFile
{
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
