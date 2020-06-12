package one.microstream.storage.types;

import java.nio.ByteBuffer;

import one.microstream.afs.AFile;
import one.microstream.afs.AReadableFile;
import one.microstream.afs.AWritableFile;
import one.microstream.chars.VarString;
import one.microstream.chars.XChars;
import one.microstream.io.BufferProvider;
import one.microstream.storage.exceptions.StorageException;
import one.microstream.storage.exceptions.StorageExceptionIoReading;

public interface StorageFile
{
	public AFile file();
	
	public long size();

	public boolean exists();
	
	
	public long readBytes(final ByteBuffer targetBuffer);
	
	public long readBytes(final ByteBuffer targetBuffer, final long position);
	
	public long readBytes(final ByteBuffer targetBuffer, final long position, final long length);
	
	
	public long readBytes(BufferProvider bufferProvider);
	
	public long readBytes(BufferProvider bufferProvider, long position);
	
	public long readBytes(BufferProvider bufferProvider, long position, long length);
	
	
	public long copyTo(AWritableFile target);
	
	public long copyTo(AWritableFile target, long sourcePosition);

	public long copyTo(AWritableFile target, long sourcePosition, long length);
	
	
		
	public static VarString assembleNameAndSize(final VarString vs, final StorageFile file)
	{
		return vs.add(file.file().identifier() + "[" + file.file().size() + "]");
	}
	
	public abstract class Abstract implements StorageFile
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final AFile file;
		
		private AWritableFile access;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Abstract(final AFile file)
		{
			super();
			this.file = file;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public AFile file()
		{
			return this.file;
		}
		
		@Override
		public final synchronized long size()
		{
			this.internalOpen();
			
			return this.access.size();
		}
		
		@Override
		public final synchronized boolean exists()
		{
			return this.file.exists();
		}
		
		@Override
		public final synchronized long readBytes(final ByteBuffer targetBuffer)
		{
			try
			{
				return this.ensureReading().readBytes(targetBuffer);
			}
			catch(final Exception e)
			{
				throw new StorageExceptionIoReading(e);
			}
		}
		
		@Override
		public final synchronized long readBytes(final ByteBuffer targetBuffer, final long position)
		{
			try
			{
				return this.ensureReading().readBytes(targetBuffer, position);
			}
			catch(final Exception e)
			{
				throw new StorageExceptionIoReading(e);
			}
		}
		
		@Override
		public final synchronized long readBytes(final ByteBuffer targetBuffer, final long position, final long length)
		{
			try
			{
				return this.ensureReading().readBytes(targetBuffer, position, length);
			}
			catch(final Exception e)
			{
				throw new StorageExceptionIoReading(e);
			}
		}
		
		@Override
		public final synchronized long readBytes(final BufferProvider bufferProvider)
		{
			try
			{
				return this.ensureReading().readBytes(bufferProvider);
			}
			catch(final Exception e)
			{
				throw new StorageExceptionIoReading(e);
			}
		}
		
		@Override
		public final synchronized long readBytes(
			final BufferProvider bufferProvider,
			final long           position
		)
		{
			try
			{
				return this.ensureReading().readBytes(bufferProvider, position);
			}
			catch(final Exception e)
			{
				throw new StorageExceptionIoReading(e);
			}
		}
		
		@Override
		public final synchronized long readBytes(
			final BufferProvider bufferProvider,
			final long           position      ,
			final long           length
		)
		{
			try
			{
				return this.ensureReading().readBytes(bufferProvider, position, length);
			}
			catch(final Exception e)
			{
				throw new StorageExceptionIoReading(e);
			}
		}
		
		@Override
		public final synchronized long copyTo(final AWritableFile target)
		{
			try
			{
				return this.ensureReading().copyTo(target);
			}
			catch(final Exception e)
			{
				throw new StorageException(e);
			}
		}
		
		@Override
		public final synchronized long copyTo(final AWritableFile target, final long sourcePosition)
		{
			try
			{
				return this.ensureReading().copyTo(target, sourcePosition);
			}
			catch(final Exception e)
			{
				throw new StorageException(e);
			}
		}

		@Override
		public final synchronized long copyTo(final AWritableFile target, final long sourcePosition, final long length)
		{
			try
			{
				return this.ensureReading().copyTo(target, sourcePosition, length);
			}
			catch(final Exception e)
			{
				throw new StorageException(e);
			}
		}
		
		protected final synchronized long copyFrom(
			final StorageFile source
		)
		{
			return source.copyTo(this.access);
		}
		
		protected final synchronized long copyFrom(
			final StorageFile source        ,
			final long        sourcePosition
		)
		{
			return source.copyTo(this.access, sourcePosition);
		}
		
		protected final synchronized long copyFrom(
			final StorageFile source        ,
			final long        sourcePosition,
			final long        length
		)
		{
			return source.copyTo(this.access, sourcePosition, length);
		}
		
		protected synchronized AReadableFile ensureReading()
		{
			return this.ensureWritable();
		}
		
		protected synchronized AWritableFile ensureWritable()
		{
			this.internalOpen();
			
			return this.access;
		}
		
		protected synchronized boolean internalIsOpen()
		{
			return this.access != null && this.access.isOpen();
		}

		protected synchronized boolean internalClose()
		{
			if(this.access == null)
			{
				return false;
			}
			
			// release closes implicitely.
			final boolean result = this.access.release();
			this.access = null;
			
			return result;
		}
		
		protected synchronized boolean internalOpen()
		{
			try
			{
				if(this.access == null)
				{
					this.access = this.file().useWriting();
				}
				
				return this.access.open();
			}
			catch(final Exception e)
			{
				throw new StorageException(e);
			}
		}
		
		@Override
		public String toString()
		{
			return XChars.systemString(this) + " (" + this.file + ")";
		}
		
	}
	
}
