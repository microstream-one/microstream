package one.microstream.storage.types;

/*-
 * #%L
 * microstream-storage
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import static one.microstream.X.notNull;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.function.Function;

import one.microstream.X;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.storage.exceptions.StorageExceptionConsistency;
import one.microstream.storage.exceptions.StorageExceptionIoReading;
import one.microstream.typing.XTypes;

public interface StorageEntityInitializer<D extends StorageLiveDataFile>
{
	public D registerEntities(XGettingSequence<? extends StorageDataInventoryFile> files, long lastFileLength);
	
	
	
	static StorageEntityInitializer<StorageLiveDataFile.Default> New(
		final StorageEntityCache.Default                                      entityCache    ,
		final Function<StorageDataInventoryFile, StorageLiveDataFile.Default> dataFileCreator
	)
	{
		return new StorageEntityInitializer.Default(
			notNull(dataFileCreator),
			notNull(entityCache)
		);
	}
	
	final class Default implements StorageEntityInitializer<StorageLiveDataFile.Default>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final Function<StorageDataInventoryFile, StorageLiveDataFile.Default> dataFileCreator;
		private final StorageEntityCache.Default                                      entityCache    ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final Function<StorageDataInventoryFile, StorageLiveDataFile.Default> dataFileCreator,
			final StorageEntityCache.Default                                      entityCache
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
		public final StorageLiveDataFile.Default registerEntities(
			final XGettingSequence<? extends StorageDataInventoryFile> files ,
			final long                                             lastFileLength
		)
		{
			return registerEntities(this.dataFileCreator, this.entityCache, files.toReversed(), lastFileLength);
		}
		
		private static StorageLiveDataFile.Default registerEntities(
			final Function<StorageDataInventoryFile, StorageLiveDataFile.Default> fileCreator    ,
			final StorageEntityCache.Default                                      entityCache    ,
			final XGettingSequence<? extends StorageDataInventoryFile>            reversedFiles  ,
			final long                                                            lastFileLength
		)
		{
			final ByteBuffer                               buffer   = allocateInitializationBuffer(reversedFiles);
			final Iterator<? extends StorageDataInventoryFile> iterator = reversedFiles.iterator();
			final int[] entityOffsets = createAllFilesOffsetsArray(buffer.capacity());
			
			final long initTime = System.currentTimeMillis();
			
			// special case handling for last/head file
			final StorageLiveDataFile.Default headFile = setupHeadFile(fileCreator.apply(iterator.next()));
			registerFileEntities(entityCache, initTime, headFile, lastFileLength, buffer, entityOffsets);
			
			// simple tail file adding iteration for all remaining (previous!) storage files
			for(StorageLiveDataFile.Default dataFile = headFile; iterator.hasNext();)
			{
				dataFile = linkTailFile(dataFile, fileCreator.apply(iterator.next()));
				registerFileEntities(entityCache, initTime, dataFile, dataFile.size(), buffer, entityOffsets);
			}
			
			XMemory.deallocateDirectByteBuffer(buffer);
			
			return headFile;
		}
		
		final static void registerFileEntities(
			final StorageEntityCache.Default  entityCache       ,
			final long                        initializationTime,
			final StorageLiveDataFile.Default file              ,
			final long                        fileActualLength  ,
			final ByteBuffer                  buffer            ,
			final int[]                       entityOffsets
		)
		{
			// entities must be indexed first to allow reverse iteration.
			final int                         entityCount = indexEntities(file, fileActualLength, buffer, entityOffsets);
			final StorageEntityCacheEvaluator entityCacheEvaluator = entityCache.entityCacheEvaluator;
			final long                        bufferStartAddress   = XMemory.getDirectByteBufferAddress(buffer);
			
			long totalFileContentLength = 0;
			
			// reverse entity iteration to register the most current version first and discard all prior versions.
			for(int i = entityCount; i --> 0;)
			{
				/*
				 * Initialization only registers the first occurrence in the reversed initialization,
				 * meaning only the most current version of every entity (identified by its ObjectId).
				 * All earlier versions are simply ignored, hence the "return false".
				 */
				if(entityCache.getEntry(Binary.getEntityObjectIdRawValue(bufferStartAddress + entityOffsets[i])) != null)
				{
					continue;
				}
				
				final long                  entityAddress = bufferStartAddress + entityOffsets[i];
				final long                  entityLength  = Binary.getEntityLengthRawValue(entityAddress);
				final StorageEntity.Default entity        = entityCache.initialCreateEntity(entityAddress);
				
				entity.updateStorageInformation(XTypes.to_int(entityLength), entityOffsets[i]);
				file.prependEntry(entity);
				totalFileContentLength += entityLength;
				
				if(entityCacheEvaluator.initiallyCacheEntity(entityCache.cacheSize(), initializationTime, entity))
				{
					entity.putCacheData(entityAddress, entityLength);
					entityCache.modifyUsedCacheSize(entityLength);
				}
			}

			// the total length of all actually registered entities is the file's content length. The rest is gaps.
			file.increaseContentLength(totalFileContentLength);
			
			// the buffer is currently limited to exactly the file size. So gapLength = limit - contentLength.
			file.registerGapLength(buffer.limit() - totalFileContentLength);
		}
				
		/**
		 * 
		 * @return the entity count.
		 */
		private static int indexEntities(
			final StorageLiveDataFile.Default file            ,
			final long                        fileActualLength,
			final ByteBuffer                  buffer          ,
			final int[]                       entityOffsets
		)
		{
			int lastEntityIndex = -1;
			
			fillBuffer(buffer, file, fileActualLength);
			
			final long bufferStartAddress = XMemory.getDirectByteBufferAddress(buffer);
			final long bufferBoundAddress = bufferStartAddress + buffer.limit();
			
			long currentItemLength;
			
			for(long address = bufferStartAddress; address < bufferBoundAddress;)
			{
				currentItemLength = Binary.getEntityLengthRawValue(address);
				
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
					throw new StorageExceptionConsistency("Zero length data item.");
				}
			}
			
			return lastEntityIndex + 1;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// utility methods //
		////////////////////
		
		private static StorageLiveDataFile.Default setupHeadFile(
			final StorageLiveDataFile.Default storageFile
		)
		{
			storageFile.next = storageFile.prev = storageFile;
			
			return storageFile;
		}

		private static StorageLiveDataFile.Default linkTailFile(
			final StorageLiveDataFile.Default currentTailFile,
			final StorageLiveDataFile.Default nextTailFile
		)
		{
			// joined in chain after current head file and before the current first
			nextTailFile.prev = (nextTailFile.next = currentTailFile).prev; // setup new element's links
			currentTailFile.prev = currentTailFile.prev.next = nextTailFile; // insert new element
			
			return nextTailFile;
		}
		
		private static int[] createAllFilesOffsetsArray(final int largestFileLength)
		{
			/*
			 * Assuming the largest file solely consists of stateless entities (only headers)
			 * guarantees to have a large enough array and a fast algorithm using it for all files.
			 * The largest file just shouldn't be allowed too large (for other reasons, too).
			 */
			return new int[largestFileLength / Binary.entityHeaderLength()];
		}
		
		private static ByteBuffer allocateInitializationBuffer(final Iterable<? extends StorageDataInventoryFile> files)
		{
			final int largestFileSize = determineLargestFileSize(files);
			
			// anything below the system's "default" buffer size (a "page", usually 4096) doesn't pay off.
			final ByteBuffer buffer = XMemory.allocateDirectNative(
				Math.max(largestFileSize, XMemory.defaultBufferSize())
			);
			
			return buffer;
		}
		
		private static void fillBuffer(
			final ByteBuffer                  buffer          ,
			final StorageLiveDataFile.Default file            ,
			final long                        fileActualLength
		)
		{
			try
			{
				buffer.clear();
				// the reason for the stupid limit is actually a single toArray() somewhere in NIO.
				buffer.limit(X.checkArrayRange(fileActualLength));
				
				file.readBytes(buffer, 0, fileActualLength);
			}
			catch(final Exception e)
			{
				throw new StorageExceptionIoReading(e);
			}
		}
		
		private static int determineLargestFileSize(final Iterable<? extends StorageDataInventoryFile> files)
		{
			int largestFileSize = -1;
			
			for(final StorageDataInventoryFile file : files)
			{
				final long fileLength = file.size();
				if(fileLength > Integer.MAX_VALUE)
				{
					/* (29.05.2018 TM)NOTE:
					 * In case someone gets here after suffering this exception:
					 * Yes, the file could be read incrementally in chunks, but while this would prevent some
					 * problems with the JDK IO buffer limitation to int, it cannot prevent all of them.
					 * Consider a single entity, probably a giant collection, whose binary form (8 bytes per
					 * element) exceeds the int limit by itself.
					 * The most reasonable strategy is:
					 * For files that should be in the range of 1-100 MB, anyway, assume int to be "infinite".
					 * Blame problems on users who chose too high a file size and on JDK who implemented
					 * the int limit.
					 */
					throw new StorageExceptionIoReading(
						"Storage file size exceeds Java technical IO limitations: " + file.identifier()
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
