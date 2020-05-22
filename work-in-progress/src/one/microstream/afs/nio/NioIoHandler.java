package one.microstream.afs.nio;

import java.nio.ByteBuffer;

import one.microstream.afs.temp.ADirectory;
import one.microstream.afs.temp.AFile;
import one.microstream.afs.temp.AReadableFile;
import one.microstream.afs.temp.AWritableFile;
import one.microstream.afs.temp.ActionReport;
import one.microstream.afs.temp.IoHandler;
import one.microstream.io.BufferProvider;


public interface NioIoHandler extends IoHandler
{
	public final class Default implements NioIoHandler
	{

		@Override
		public long length(final AFile file)
		{
			// FIXME IoHandler#length()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public boolean openReading(final AReadableFile file)
		{
			// FIXME IoHandler#openReading()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public boolean isOpenReading(final AReadableFile file)
		{
			// FIXME IoHandler#isOpenReading()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public boolean close(final AReadableFile file)
		{
			// FIXME IoHandler#close()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public boolean isClosed(final AReadableFile file)
		{
			// FIXME IoHandler#isClosed()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public boolean openWriting(final AWritableFile file)
		{
			// FIXME IoHandler#openWriting()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public boolean isOpenWriting(final AWritableFile file)
		{
			// FIXME IoHandler#isOpenWriting()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public boolean ensure(final ADirectory file)
		{
			// FIXME IoHandler#ensure()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public boolean ensure(final AReadableFile file)
		{
			// FIXME IoHandler#ensure()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public ActionReport ensureWritable(final AWritableFile file)
		{
			// FIXME IoHandler#ensureWritable()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public ByteBuffer readBytes(final AReadableFile sourceFile)
		{
			// FIXME IoHandler#readBytes()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public ByteBuffer readBytes(final AReadableFile sourceFile, final long position)
		{
			// FIXME IoHandler#readBytes()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public ByteBuffer readBytes(final AReadableFile sourceFile, final long position, final long length)
		{
			// FIXME IoHandler#readBytes()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public long readBytes(final AReadableFile sourceFile, final ByteBuffer targetBuffer)
		{
			// FIXME IoHandler#readBytes()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public long readBytes(final AReadableFile sourceFile, final ByteBuffer targetBuffer, final long position)
		{
			// FIXME IoHandler#readBytes()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public long readBytes(final AReadableFile sourceFile, final ByteBuffer targetBuffer, final long position, final long length)
		{
			// FIXME IoHandler#readBytes()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public long readBytes(final AReadableFile sourceFile, final BufferProvider bufferProvider)
		{
			// FIXME IoHandler#readBytes()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public long readBytes(final AReadableFile sourceFile, final BufferProvider bufferProvider, final long position)
		{
			// FIXME IoHandler#readBytes()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public long readBytes(final AReadableFile sourceFile, final BufferProvider bufferProvider, final long position, final long length)
		{
			// FIXME IoHandler#readBytes()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public long copyTo(final AReadableFile sourceFile, final AWritableFile target)
		{
			// FIXME IoHandler#copyTo()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public long copyTo(final AReadableFile sourceFile, final AWritableFile target, final long sourcePosition)
		{
			// FIXME IoHandler#copyTo()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public long copyTo(final AReadableFile sourceFile, final AWritableFile target, final long sourcePosition, final long length)
		{
			// FIXME IoHandler#copyTo()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public long writeBytes(final AWritableFile targetFile, final Iterable<? extends ByteBuffer> sourceBuffers)
		{
			// FIXME IoHandler#writeBytes()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public void moveFile(final AWritableFile sourceFile, final AWritableFile targetFile)
		{
			// FIXME IoHandler#moveFile()
			throw new one.microstream.meta.NotImplementedYetError();
		}

		@Override
		public boolean deleteFile(final AWritableFile file)
		{
			// FIXME IoHandler#deleteFile()
			throw new one.microstream.meta.NotImplementedYetError();
		}
		
	}
}
