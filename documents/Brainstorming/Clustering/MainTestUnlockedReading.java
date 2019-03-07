package one.microstream.storage.types;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import one.microstream.concurrency.XThreads;
import one.microstream.files.XFiles;
import one.microstream.low.XVM;
import one.microstream.meta.XDebug;


@SuppressWarnings("resource") // resource closed internally by FileChannel (JDK tricking Java compiler ^^)
public class MainTestUnlockedReading
{
	public static void main(final String[] args) throws IOException
	{
		doit(new File("D:\\WriteTest.bin"));
	}

	static final int START = 40;
	static final int BOUND = 100;
	static final int THREAD_COUNT = 1;
	
	static void doit(final File file) throws IOException
	{
		XFiles.ensureWriteableFile(file);

		// will cause an exception in the reader despite not trying for a lock there (implicit mandatory lock handling)
//		final FileChannel rwChannel = openFileChannelRwLocked(file);
		
		// allows writer and reader to access the file simultaneously (obviously, since there's no locking at all)
		final FileChannel rwChannel = new RandomAccessFile(file, "rw").getChannel();
		
		rwChannel.truncate(0);
		rwChannel.position(0);
		
		final ByteBuffer bb = ByteBuffer.allocateDirect(4);
		bb.limit(4);
		final long address = XVM.getDirectByteBufferAddress(bb);
		
		for(int i = 0; i < THREAD_COUNT; i++)
		{
			final String name = "Reader " + (i + 1);
			XDebug.println("Starting " + name);
			XThreads.start(() -> {
				readFromFile2(name, file);
			});
		}
		
		for(int i = START; i < BOUND; i++)
		{
			XDebug.println("Writing " + i);
			bb.position(0);
			XVM.set_int(address, i);
			rwChannel.write(bb);
			XThreads.sleep(250);
		}
		XDebug.println("Writing completed.");
	}
	
	static void readFromFile2(final String name, final File file)
	{
		try
		{
			readFromFile(name, file);
		}
		catch(final IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	static void readFromFile(final String name, final File file) throws IOException
	{
		final FileChannel channel = new RandomAccessFile(file, "r").getChannel();
		
		long currentProgress = 0;
		final ByteBuffer bb = ByteBuffer.allocateDirect(4);
		final long address = XVM.getDirectByteBufferAddress(bb);
		bb.limit(4);
		
		int readValue = -1;
		while(readValue < BOUND)
		{
			bb.position(0);
			while(channel.size() - bb.remaining() < currentProgress)
			{
				XThreads.sleep(100);
			}
			channel.read(bb, currentProgress);
			
			readValue = XVM.get_int(address);
			XDebug.println(name + " read " + readValue);
			currentProgress += bb.limit();
		}

		XDebug.println(name + " reading complete.");
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// utils //
	//////////
	
	public static FileChannel openFileChannelRwLocked(final File file)
	{
		// the file is always completely and unshared locked.
		final FileLock lock;
		FileChannel channel = null;
		try
		{
			// resource closed internally by FileChannel (JDK tricking Java compiler ^^)
			channel = new RandomAccessFile(file, "rw").getChannel();

			/*
			 * Tests showed that Java file locks even on Windows don't work properly:
			 * They only prevent other Java processes from acquiring another lock.
			 * But other applications (e.g. Hexeditor) can still open and write to the file.
			 * This basically makes any attempt to secure the file useless.
			 * Not only on linux which seems to be complete crap when it comes to locking files,
			 * but also on windows.
			 * As there is no alternative available and it at least works within Java, it is kept nevertheless.
			 */
			lock = channel.tryLock();
			if(lock == null)
			{
				throw new RuntimeException("File seems to be already locked: " + file);
			}
		}
		catch(final Exception e)
		{
			XFiles.closeSilent(channel);
			// (28.06.2014)EXCP: proper exception
			throw new RuntimeException("Cannot obtain lock for file " + file, e);
		}

		return channel;
	}
	
}
