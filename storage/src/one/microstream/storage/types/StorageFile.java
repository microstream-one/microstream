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
	public default String identifier()
	{
		return this.file().toPathString();
	}
	
	public AFile file();
	
	public long size();

	public boolean exists();
	
	
	public long readBytes(final ByteBuffer targetBuffer);
	
	public long readBytes(final ByteBuffer targetBuffer, final long position);
	
	public long readBytes(final ByteBuffer targetBuffer, final long position, final long length);
	
	
	public long readBytes(BufferProvider bufferProvider);
	
	public long readBytes(BufferProvider bufferProvider, long position);
	
	public long readBytes(BufferProvider bufferProvider, long position, long length);
	
	
	public long writeBytes(Iterable<? extends ByteBuffer> buffers);
	
	
//	public void pull(AWritableFile fileToMove);
	
	
	public long copyTo(StorageFile target);
	
	public long copyTo(StorageFile target, long sourcePosition);

	public long copyTo(StorageFile target, long sourcePosition, long length);
	
	
	public long copyTo(AWritableFile target);
	
	public long copyTo(AWritableFile target, long sourcePosition);

	public long copyTo(AWritableFile target, long sourcePosition, long length);
	
	
	public long copyFrom(AReadableFile source);
	
	public long copyFrom(AReadableFile source, long sourcePosition);

	public long copyFrom(AReadableFile source, long sourcePosition, long length);
	
	
	public boolean delete();

	public void moveTo(AWritableFile target);
	
		
		
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
		
		private AWritableFile writeAccess;
		private AReadableFile readAccess ;
		
		
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
			return this.file().size();
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
				return this.ensureReadable().readBytes(targetBuffer);
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
				return this.ensureReadable().readBytes(targetBuffer, position);
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
				return this.ensureReadable().readBytes(targetBuffer, position, length);
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
				return this.ensureReadable().readBytes(bufferProvider);
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
				return this.ensureReadable().readBytes(bufferProvider, position);
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
				return this.ensureReadable().readBytes(bufferProvider, position, length);
			}
			catch(final Exception e)
			{
				throw new StorageExceptionIoReading(e);
			}
		}
		

		@Override
		public final synchronized long writeBytes(final Iterable<? extends ByteBuffer> buffers)
		{
			try
			{
				return this.ensureWritable().writeBytes(buffers);
			}
			catch(final Exception e)
			{
				throw new StorageExceptionIoReading(e);
			}
		}
		
//		@Override
//		public final synchronized void pull(final AWritableFile fileToMove)
//		{
//			try
//			{
//				fileToMove.moveTo(this.ensureWritable());
//			}
//			catch(final Exception e)
//			{
//				throw new StorageExceptionIoReading(e);
//			}
//		}
				
		@Override
		public final synchronized long copyTo(
			final StorageFile target
		)
		{
			return target.copyFrom(this.ensureReadable());
		}
		
		@Override
		public final synchronized long copyTo(
			final StorageFile target        ,
			final long        sourcePosition
		)
		{
			return target.copyFrom(this.ensureReadable(), sourcePosition);
		}

		@Override
		public final synchronized long copyTo(
			final StorageFile target        ,
			final long        sourcePosition,
			final long        length
		)
		{
			return target.copyFrom(this.ensureReadable(), sourcePosition, length);
		}
		
		@Override
		public final synchronized long copyTo(
			final AWritableFile target
		)
		{
			try
			{
				return target.copyFrom(this.ensureReadable());
			}
			catch(final Exception e)
			{
				throw new StorageException(e);
			}
		}
		
		@Override
		public final synchronized long copyTo(
			final AWritableFile target        ,
			final long          sourcePosition
		)
		{
			try
			{
				return target.copyFrom(this.ensureReadable(), sourcePosition);
			}
			catch(final Exception e)
			{
				throw new StorageException(e);
			}
		}

		@Override
		public final synchronized long copyTo(
			final AWritableFile target        ,
			final long          sourcePosition,
			final long          length
		)
		{
			try
			{
				target.ensureExists();
				return target.copyFrom(this.ensureReadable(), sourcePosition, length);
			}
			catch(final Exception e)
			{
				throw new StorageException(e);
			}
		}
				
		@Override
		public final synchronized long copyFrom(
			final AReadableFile source
		)
		{
			try
			{
				return source.copyTo(this.ensureWritable());
			}
			catch(final Exception e)
			{
				throw new StorageException(e);
			}
		}
		
		@Override
		public final synchronized long copyFrom(
			final AReadableFile source        ,
			final long          sourcePosition
		)
		{
			try
			{
				return source.copyTo(this.ensureWritable(), sourcePosition);
			}
			catch(final Exception e)
			{
				throw new StorageException(e);
			}
		}

		@Override
		public final synchronized long copyFrom(
			final AReadableFile source        ,
			final long          sourcePosition,
			final long          length
		)
		{
			try
			{
				return source.copyTo(this.ensureWritable(), sourcePosition, length);
			}
			catch(final Exception e)
			{
				throw new StorageException(e);
			}
		}
				
		public final synchronized void truncate(final long newLength)
		{
			this.ensureWritable().truncate(newLength);
		}
		
		@Override
		public final synchronized boolean delete()
		{
			return this.ensureWritable().delete();
		}
		
		@Override
		public final synchronized void moveTo(final AWritableFile target)
		{
			this.ensureWritable().moveTo(target);
		}
		
		protected synchronized AReadableFile ensureReadable()
		{
			if(this.file.fileSystem().isWritable())
			{
				return this.ensureWritable();
			}
			
			this.internalOpenReading();
			
			return this.readAccess;
		}
		
		protected synchronized AWritableFile ensureWritable()
		{
			this.internalOpenWriting();
			
			return this.writeAccess;
		}
		
		public synchronized boolean isOpen()
		{
			return this.writeAccess != null && this.writeAccess.isOpen();
		}

		public synchronized boolean close()
		{
			if(this.writeAccess == null)
			{
				return false;
			}
			
			// release closes implicitely.
			final boolean result = this.writeAccess.release();
			this.writeAccess = null;
			
			return result;
		}
		
		protected synchronized boolean internalOpenWriting()
		{
			try
			{
				if(this.writeAccess == null || this.writeAccess.isRetired())
				{
					this.writeAccess = this.file().useWriting();
					this.readAccess = this.writeAccess;
				}
				
				this.writeAccess.ensureExists();
				return this.writeAccess.open();
			}
			catch(final Exception e)
			{
				throw new StorageException(e);
			}
		}
		
		protected synchronized boolean internalOpenReading()
		{
			try
			{
				if(this.readAccess == null || this.readAccess.isRetired())
				{
					this.writeAccess = null;
					this.readAccess = this.file().useReading();
				}
				
				this.readAccess.ensureExists();
				return this.readAccess.open();
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
