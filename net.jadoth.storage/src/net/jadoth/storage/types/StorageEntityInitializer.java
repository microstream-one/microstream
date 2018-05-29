package net.jadoth.storage.types;

import static net.jadoth.X.notNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.function.Function;

import net.jadoth.X;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.memory.Memory;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.storage.exceptions.StorageExceptionIoReading;

public interface StorageEntityInitializer<D extends StorageDataFile<?>>
{
	public D registerEntities(
		XGettingSequence<? extends StorageInventoryFile> reversedFiles ,
		long                                             lastFileLength
	);
	
	
	
	static StorageEntityInitializer<StorageDataFile.Implementation> New(
		final StorageEntityCache.Implementation                              entityCache    ,
		final Function<StorageInventoryFile, StorageDataFile.Implementation> dataFileCreator
	)
	{
		return new StorageEntityInitializer.Implementation(
			notNull(dataFileCreator),
			notNull(entityCache)
		);
	}
	
	final class Implementation implements StorageEntityInitializer<StorageDataFile.Implementation>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final Function<StorageInventoryFile, StorageDataFile.Implementation> dataFileCreator;
		private final StorageEntityCache.Implementation                              entityCache    ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(
			final Function<StorageInventoryFile, StorageDataFile.Implementation> dataFileCreator,
			final StorageEntityCache.Implementation                              entityCache
		)
		{
			super();
			this.dataFileCreator = dataFileCreator;
			this.entityCache     = entityCache    ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final StorageDataFile.Implementation registerEntities(
			final XGettingSequence<? extends StorageInventoryFile> reversedFiles ,
			final long                                             lastFileLength
		)
		{
			final ByteBuffer                               buffer   = allocateInitializationBuffer(reversedFiles);
			final Iterator<? extends StorageInventoryFile> iterator = reversedFiles.iterator();

			return registerEntities(this.dataFileCreator, this.entityCache, buffer, iterator, lastFileLength);
		}
		
		private static StorageDataFile.Implementation registerEntities(
			final Function<StorageInventoryFile, StorageDataFile.Implementation> fileCreator    ,
			final StorageEntityCache.Implementation                              entityCache    ,
			final ByteBuffer                                                     fileReadBuffer ,
			final Iterator<? extends StorageInventoryFile>                       revFileIterator,
			final long                                                           lastFileLength
		)
		{
			final int[] entityOffsets = createOffsetsArray(fileReadBuffer.capacity());
			
			// special case handling for last/head file
			final StorageDataFile.Implementation headFile = setupHeadFile(fileCreator.apply(revFileIterator.next()));
			registerFileEntities(entityCache, headFile, lastFileLength, fileReadBuffer, entityOffsets);
			
			// simple tail file adding iteration for all remaining (previous!) storage files
			for(StorageDataFile.Implementation dataFile = headFile; revFileIterator.hasNext();)
			{
				registerFileEntities(entityCache, dataFile, dataFile.length(), fileReadBuffer, entityOffsets);
				dataFile = linkTailFile(dataFile, fileCreator.apply(revFileIterator.next()));
			}
			
			return headFile;
		}
		
		final static void registerFileEntities(
			final StorageEntityCache.Implementation entityCache     ,
			final StorageDataFile.Implementation    file            ,
			final long                              fileActualLength,
			final ByteBuffer                        buffer          ,
			final int[]                             entityOffsets
		)
		{
			// entities must be indexed first to allow reverse iteration.
			final int entityCount = indexEntities(file, fileActualLength, buffer, entityOffsets);
			
			long totalContentLength = 0;

			// reverse entity iteration to register the most current version first and discard all prior versions.
			final long bufferStartAddress = Memory.getDirectByteBufferAddress(buffer);
			for(int i = entityCount; i --> 0;)
			{
				if(entityCache.initialRegisterEntity(bufferStartAddress + entityOffsets[i], entityOffsets[i]))
				{
					totalContentLength += BinaryPersistence.getEntityLength(bufferStartAddress + entityOffsets[i]);
				}
			}

			// the total length of all actually registered entities is the file's content length. The rest is gaps.
			file.increaseContentLength(totalContentLength);
			
			// the buffer is currently limited to exactely the file size. So gapLength = limit - contentLength.
			file.registerGapLength(buffer.limit() - totalContentLength);
		}
		
		/**
		 * 
		 * @param file
		 * @param buffer
		 * @param entityOffsets
		 * @return the entity count.
		 */
		private static int indexEntities(
			final StorageDataFile.Implementation file            ,
			final long                           fileActualLength,
			final ByteBuffer                     buffer          ,
			final int[]                          entityOffsets
		)
		{
			int lastEntityIndex = -1;
			
			fillBuffer(buffer, file, fileActualLength);
			
			final long bufferStartAddress = Memory.getDirectByteBufferAddress(buffer);
			final long bufferBoundAddress = bufferStartAddress + buffer.limit();
			
			long currentItemLength;
			
			for(long address = bufferStartAddress; address < bufferBoundAddress;)
			{
				currentItemLength = BinaryPersistence.getEntityLength(address);
				
				if(currentItemLength > 0)
				{
					// handle actual entity
					entityOffsets[++lastEntityIndex] = (int)(address - bufferStartAddress);
					address += currentItemLength;
				}
				else if(currentItemLength < 0)
				{
					// comments (indicated by negative length) just get skipped.
					// note that gap length gets registered for the file at the end arithmetically
					address -= currentItemLength;
				}
				else
				{
					// entity length may never be 0 or the iteration will hang forever
					throw new RuntimeException("Zero length data item."); // (29.08.2014)EXCP: proper exception
				}
			}
			
			return lastEntityIndex + 1;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// utility methods //
		////////////////////
		
		private static StorageDataFile.Implementation setupHeadFile(
			final StorageDataFile.Implementation storageFile
		)
		{
			storageFile.next = storageFile.prev = storageFile;
			
			return storageFile;
		}

		private static StorageDataFile.Implementation linkTailFile(
			final StorageDataFile.Implementation currentTailFile,
			final StorageDataFile.Implementation nextTailFile
		)
		{
			// joined in chain after current head file and before the current first
			nextTailFile.prev = (nextTailFile.next = currentTailFile).prev; // setup new element's links
			currentTailFile.prev = currentTailFile.prev.next = nextTailFile; // insert new element
			
			return nextTailFile;
		}
		
		private static int[] createOffsetsArray(final int fileLength)
		{
			return new int[fileLength / BinaryPersistence.entityHeaderLength()];
		}
		
		private static ByteBuffer allocateInitializationBuffer(final Iterable<? extends StorageInventoryFile> files)
		{
			final int largestFileSize = determineLargestFileSize(files);
			
			// anything below the system's "default" buffer size (a "page", usually 4096) doesn't pay off.
			final ByteBuffer buffer = ByteBuffer.allocateDirect(
				Math.max(largestFileSize, Memory.defaultBufferSize())
			);
			
			return buffer;
		}
		
		private static void fillBuffer(
			final ByteBuffer                     buffer          ,
			final StorageDataFile.Implementation file            ,
			final long                           fileActualLength
		)
		{
			try
			{
				final FileChannel fileChannel = file.fileChannel();
				fileChannel.position(0);

				buffer.clear();
				// the reason for the stupid limit is actually a single clumsy toArray() somewhere in NIO.
				buffer.limit(X.checkArrayRange(fileActualLength));

				// loop is guaranteed to terminate as it depends on the buffer size and the file length
				do
				{
					fileChannel.read(buffer);
				}
				while(buffer.hasRemaining());
			}
			catch(final IOException e)
			{
				throw new StorageExceptionIoReading(e);
			}
		}
		
		private static int determineLargestFileSize(final Iterable<? extends StorageInventoryFile> files)
		{
			int largestFileSize = -1;
			
			for(final StorageInventoryFile file : files)
			{
				final long fileLength = file.length();
				if(fileLength > Integer.MAX_VALUE)
				{
					/* (29.05.2018 TM)NOTE:
					 * In case someone gets here after suffering this exception:
					 * Yes, the file could be read incrementally in chunks, but while this would prevent some
					 * problems with the moronic JDK IO buffer limitation to int, it cannot prevent all of them.
					 * Consider a single entity, probabaly a giant collection, whose binary form (8 bytes per
					 * element) exceeds the int limit by itself.
					 * The most reasonable strategy is:
					 * For files that should be in the range of 1-100 MB, anyway, assume int to be "infinite".
					 * Blame problems on users who chose too high a file size and on JDK devs who implemented
					 * the moronicly short-sighted and completely unnecessary int limit.
					 * And wait for a proper solution, of course. Maybe a self-built tailored IO via JNI.
					 */
					throw new StorageExceptionIoReading(
						"File size exceeds Java technical IO limitations: " + file.file()
					);
				}
				
				if(fileLength > largestFileSize)
				{
					// cast safety is guaranteed by the validation logic above.
					largestFileSize = (int)fileLength;
				}
			}
			
			return largestFileSize;
		}
		
	}

}
