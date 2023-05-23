package one.microstream.storage.types;

/*-
 * #%L
 * MicroStream Storage
 * %%
 * Copyright (C) 2019 - 2023 MicroStream Software
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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.zip.CRC32;

import org.slf4j.Logger;

import one.microstream.collections.ArrayView;
import one.microstream.memory.XMemory;
import one.microstream.util.logging.Logging;

public interface StorageStartupFileIndexer {

	/**
	 * create index for the supplied storage file.
	 * 
	 * @param liveDataFile StorageLiveDataFile to be indexed.
	 * @param indexFile    output StorageStartupIndexFile
	 */
	void indexStorageFile(StorageLiveDataFile liveDataFile, StorageStartupIndexFile indexFile);
	
	
	/**
	 * read and import index data into the storage.
	 * 
	 * @param liveDataFile StorageLiveDataFile import target
	 * @param indexFile    StorageStartupIndexFile to be imported
	 * @param entityCache  the entity cache
	 */
	void initWithIndexFile(StorageLiveDataFile.Default liveDataFile, StorageStartupIndexFile indexFile, StorageEntityCache.Default entityCache);
	
	/**
	 * Check if the supplied StorageStartupIndexFile is up-to-date with its
	 * associated StorageLiveDataFile.
	 * 
	 * @param storageLiveDataFile a StorageLiveDataFile
	 * @param indexFile a StorageStartupIndexFile
	 * @return true if the indexFile matches the storageLiveDataFile for initialization, otherwise false.
	 */
	boolean isUpToDate(StorageLiveDataFile storageLiveDataFile, StorageStartupIndexFile indexFile);

	public class Default implements StorageStartupFileIndexer
	{
		private final static Logger logger = Logging.getLogger(StorageStartupFileIndexer.class);
		
		private static final int MAX_ENTITY_SIZE          = 21;
			
		
		private static final class FileHeader
		{
			private static final int FILE_HEADER_CHANNELINDEX_OFFSET    = 0;
			private static final int FILE_HEADER_FILENUMBER_OFFSET      = FILE_HEADER_CHANNELINDEX_OFFSET    + XMemory.byteSize_int();
			private static final int FILE_HEADER_FILESIZE_OFFSET        = FILE_HEADER_FILENUMBER_OFFSET      + XMemory.byteSize_long();
			private static final int FILE_HEADER_DATALENGTH_OFFSET      = FILE_HEADER_FILESIZE_OFFSET        + XMemory.byteSize_long();
			private static final int FILE_HEADER_TYPEINFILECOUNT_OFFSET = FILE_HEADER_DATALENGTH_OFFSET      + XMemory.byteSize_long();
			private static final int FILE_HEADER_ENTITYCOUNT_OFFSET     = FILE_HEADER_TYPEINFILECOUNT_OFFSET + XMemory.byteSize_int();
			private static final int FILE_HEADER_CHECKSUMM_OFFSET       = FILE_HEADER_ENTITYCOUNT_OFFSET     + XMemory.byteSize_long();
			
			private static final int FILE_HEADER_SIZE         = 48;
			
			private final int  channelIndex;
			private final long fileNumber;
			private final long dataFileSize;
			private final long liveDataLength;
			private final int  typeInFileCount;
			private final long entityCount;
			private final long contentCheckSum;
								
			public FileHeader(
				final int  channelIndex,
				final long fileNumber,
				final long dataFileSize,
				final long liveDataLength,
				final int  typeInFileCount,
				final long entityCount,
				final long contentCheckSum
			)
			{
				super();
				this.channelIndex    = channelIndex;
				this.fileNumber      = fileNumber;
				this.dataFileSize    = dataFileSize;
				this.liveDataLength  = liveDataLength;
				this.typeInFileCount = typeInFileCount;
				this.entityCount     = entityCount;
				this.contentCheckSum = contentCheckSum;
			}

			public FileHeader(final long startAddress)
			{
				super();
				
				this.channelIndex    = XMemory.get_int(startAddress  + FILE_HEADER_CHANNELINDEX_OFFSET);
				this.fileNumber      = XMemory.get_long(startAddress + FILE_HEADER_FILENUMBER_OFFSET);
				this.dataFileSize    = XMemory.get_long(startAddress + FILE_HEADER_FILESIZE_OFFSET);
				this.liveDataLength  = XMemory.get_long(startAddress + FILE_HEADER_DATALENGTH_OFFSET);
				this.typeInFileCount = XMemory.get_int(startAddress  + FILE_HEADER_TYPEINFILECOUNT_OFFSET);
				this.entityCount     = XMemory.get_long(startAddress + FILE_HEADER_ENTITYCOUNT_OFFSET);
				this.contentCheckSum = XMemory.get_long(startAddress + FILE_HEADER_CHECKSUMM_OFFSET);
			}
			
			public long write(final long startAddress)
			{
				XMemory.set_int(startAddress  + FILE_HEADER_CHANNELINDEX_OFFSET   , this.channelIndex);
				XMemory.set_long(startAddress + FILE_HEADER_FILENUMBER_OFFSET     , this.fileNumber);
				XMemory.set_long(startAddress + FILE_HEADER_FILESIZE_OFFSET       , this.dataFileSize);
				XMemory.set_long(startAddress + FILE_HEADER_DATALENGTH_OFFSET     , this.liveDataLength);
				XMemory.set_int(startAddress  + FILE_HEADER_TYPEINFILECOUNT_OFFSET, this.typeInFileCount);
				XMemory.set_long(startAddress + FILE_HEADER_ENTITYCOUNT_OFFSET    , this.entityCount);
				XMemory.set_long(startAddress + FILE_HEADER_CHECKSUMM_OFFSET      , this.contentCheckSum);
		
				return FILE_HEADER_SIZE;
			}

			@Override
			public String toString()
			{
				return "FileHeader [channelIndex=" + this.channelIndex + ", fileNumber=" + this.fileNumber + ", dataFileSize="
						+ this.dataFileSize + ", liveDataLength=" + this.liveDataLength + ", typeInFileCount=" + this.typeInFileCount
						+ ", entityCount=" + this.entityCount + ", contentCheckSum=" + this.contentCheckSum + "]";
			}
			
		}
		
		
		private static class TypeInFileHeader
		{
			private static final int TYPEID_OFFSET      = 0;
			private static final int VARLENGHT_OFFSET   = TYPEID_OFFSET      + XMemory.byteSize_long();
			private static final int FILENUMBER_OFFSET  = VARLENGHT_OFFSET   + XMemory.byteSize_boolean();
			private static final int BASEOID_OFFSET     = FILENUMBER_OFFSET  + XMemory.byteSize_long();
			private static final int ENTITYCOUNT_OFFSET = BASEOID_OFFSET     + XMemory.byteSize_long();
			private static final int SEGMENTSIZE_OFFSET = ENTITYCOUNT_OFFSET + XMemory.byteSize_int();
			
			private static final int TYPE_IN_FILE_HEADER_SIZE = 34;
			
			final long    typeId;
			final boolean variableLength;
			final long    fileNumber;
			final long    baseOid;
			final int     entityCount;
			final int     segmentSize;
						
			public TypeInFileHeader(
				final long    typeId,
				final boolean variableLength,
				final long    fileNumber,
				final long    baseOid,
				final int     entityCount,
				final int     segmentSize
			)
			{
				super();
				this.typeId = typeId;
				this.variableLength = variableLength;
				this.fileNumber = fileNumber;
				this.baseOid = baseOid;
				this.entityCount = entityCount;
				this.segmentSize = segmentSize;
			}
			
			public TypeInFileHeader(final long startAddress)
			{
				super();
				
				this.typeId         = XMemory.get_long(startAddress + TYPEID_OFFSET);
				this.variableLength = XMemory.get_boolean(startAddress + VARLENGHT_OFFSET);
				this.fileNumber     = XMemory.get_long(startAddress + FILENUMBER_OFFSET);
				this.baseOid        = XMemory.get_long(startAddress + BASEOID_OFFSET);
				this.entityCount    = XMemory.get_int(startAddress  + ENTITYCOUNT_OFFSET);
				this.segmentSize    = XMemory.get_int(startAddress  + SEGMENTSIZE_OFFSET);
			}
			
			public long write(final long startAddress)
			{
				XMemory.set_long(startAddress + TYPEID_OFFSET     , this.typeId);
				XMemory.set_boolean(startAddress + VARLENGHT_OFFSET  , this.variableLength);
				XMemory.set_long(startAddress + FILENUMBER_OFFSET , this.fileNumber);
				XMemory.set_long(startAddress + BASEOID_OFFSET    , this.baseOid);
				XMemory.set_int(startAddress  + ENTITYCOUNT_OFFSET, this.entityCount);
				XMemory.set_int(startAddress  + SEGMENTSIZE_OFFSET, this.segmentSize);
				
				
				return TYPE_IN_FILE_HEADER_SIZE;
			}
		}
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		public Default()
		{
			super();
		}
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public void indexStorageFile(final StorageLiveDataFile liveDataFile, final StorageStartupIndexFile indexFile)
		{
			logger.debug("indexing storage file {}", liveDataFile.identifier());
			
			final HashMap<TypeInFile, List<StorageEntity.Default>> map = new HashMap<>();
			final int entityCount = this.collectTypesInFile(liveDataFile, map);
					
			//provide buffers
			//estimate worst case size to ensure just one buffer allocation
			final int headerSize = TypeInFileHeader.TYPE_IN_FILE_HEADER_SIZE * map.size();
			final int entitySize = MAX_ENTITY_SIZE * entityCount;

			final ByteBuffer buffer = XMemory.allocateDirectNative(headerSize + entitySize + FileHeader.FILE_HEADER_SIZE);
			final long startAddress = XMemory.getDirectByteBufferAddress(buffer);
			long nextAddress = startAddress + FileHeader.FILE_HEADER_SIZE;
			
			long entityInFileCount = 0;
						
			for (final Entry<TypeInFile, List<one.microstream.storage.types.StorageEntity.Default>> entry : map.entrySet())
			{
				final TypeInFile key = entry.getKey();
				final List<one.microstream.storage.types.StorageEntity.Default> entities = entry.getValue();
				
				entityInFileCount += entities.size();
				
				//Find lowest ObjectID to calculate "offset"
				final long minOid = this.getMinOid(entities);
				final boolean variableLength = key.type.typeHandler().hasPersistedVariableLength();
										
				//TypeInFile header is written after entities, so increase write address by header size
				final long segmentStartAddress = nextAddress;
				nextAddress += TypeInFileHeader.TYPE_IN_FILE_HEADER_SIZE;
															
				for (final StorageEntity.Default entity : entities)
				{
					nextAddress = this.putEntity(entity, minOid, variableLength, nextAddress);
				}
								
				final TypeInFileHeader typeInFileHeader = new TypeInFileHeader(
					key.type.typeId,
					variableLength,
					key.file.number(),
					minOid,
					entities.size(),
					(int) (nextAddress - segmentStartAddress));
				
				typeInFileHeader.write(segmentStartAddress);
			}
			
			final long checksum = this.calculateChecksum(buffer ,FileHeader.FILE_HEADER_SIZE, (int) (nextAddress - startAddress));
			
			final FileHeader fileHeader = new FileHeader(
				liveDataFile.channelIndex(),
				liveDataFile.number(),
				liveDataFile.file().size(),
				liveDataFile.dataLength(),
				map.size(),
				entityInFileCount,
				checksum);
			
			fileHeader.write(startAddress);
						
			indexFile.writeBytes(new ArrayView<>(buffer));
			indexFile.close();
			
			logger.debug("Wrote File Header: {} for file {}", fileHeader, indexFile.identifier());
		}


		private long calculateChecksum(final ByteBuffer buffer, final int startPosition, final int limit)
		{
			buffer.position(startPosition);
			buffer.limit(limit);
			final CRC32 crc32 = new CRC32();
			crc32.update(buffer);
			buffer.flip();
			return crc32.getValue();
		}
		
		private long getMinOid(final List<one.microstream.storage.types.StorageEntity.Default> entities)
		{
			long minOid = Long.MAX_VALUE;
			for (final StorageEntity.Default entity : entities)
			{
				final long oid = entity.objectId;
				
				if(oid < minOid)
				{
					minOid = oid;
				}
			}
			return minOid;
		}
				
		/**
		 * Write entity to supplied memory address
		 * 
		 * @param entity the StorageEntity
		 * @param minOid minimum ObjectId of TypeInFile
		 * @param startAddress first address to write to
		 * @return address next address after written entity
		 */
		private long putEntity(final StorageEntity.Default entity, final long minOid, final boolean variableLength, final long startAddress)
		{
			final long objectIDOffset = entity.objectId() - minOid;
			final int requiredBytesOid = this.requiredBytesOne(objectIDOffset);
			final int requiredBytesFilePos = this.requiredBytesOne(entity.storagePosition());
				
			//No data length required for constant length types
			final int requiredBytesLength = variableLength ? this.requiredBytesOne(entity.dataLength()) : 0;
										
			final byte entityMetaData = (byte) (
					  (((requiredBytesLength  - 1) << 5 ) & 0x60)
					| (((requiredBytesFilePos - 1) << 3 ) & 0x18)
					| (((requiredBytesOid     - 1)      ) & 0x07));
							
			long nextAddress = startAddress;
			
			XMemory.set_byte(nextAddress++, entityMetaData);
			
			for(int i = 0; i <= requiredBytesOid; i++)
			{
				XMemory.set_byte(nextAddress, (byte) (objectIDOffset >> i*8));
				nextAddress++;
			}
			
			for(int i = 0; i <= requiredBytesFilePos; i++)
			{
				XMemory.set_byte(nextAddress, (byte) (entity.storagePosition() >> i*8));
				nextAddress++;
			}
			
			if(entity.typeInFile.type.typeHandler().hasPersistedVariableLength())
			{
				for(int i = 0; i <= requiredBytesLength; i++)
				{
					XMemory.set_byte(nextAddress, (byte) (entity.dataLength() >> i*8));
					nextAddress++;
				}
			}
			
			return nextAddress;
		}
		
		private int collectTypesInFile(
			final StorageLiveDataFile liveDataFile,
			final HashMap<TypeInFile, List<StorageEntity.Default>> map
		)
		{
			final StorageLiveDataFile.Default srcFile = (StorageLiveDataFile.Default)liveDataFile;
			final StorageEntity.Default head =  srcFile.head;
			final StorageEntity.Default tail =  srcFile.tail;
								
			int entityCount = 0;
			for(StorageEntity.Default entity = head; (entity = entity.fileNext) != tail;)
			{
				final TypeInFile tif = entity.typeInFile;
				
				final List<StorageEntity.Default> tifBucket = map.computeIfAbsent(tif, k -> new ArrayList<>());
				tifBucket.add(entity);
				entityCount++;
			}
			
			return entityCount;
		}


		/**
		 * get the minimum number of bytes to encode the supplied value;
		 * at least one is returned.
		 * @param number to be encoded
		 * @return byte required to encode value; at least one
		 */
		private byte requiredBytesOne(long number)
		{
			byte requiredBytes = 0;
			do { number >>= 8; requiredBytes ++; } while(number > 0);
			return requiredBytes;
		}


		@Override
		public void initWithIndexFile(
			final StorageLiveDataFile.Default liveDataFile,
			final StorageStartupIndexFile indexFile,
			final StorageEntityCache.Default entityCache
		)
		{
			logger.debug("reading index file {}", indexFile.identifier());
			
			final ByteBuffer fb = XMemory.allocateDirectNative(indexFile.size());
			indexFile.readBytes(fb);
			indexFile.close();
			
			fb.flip();
					
			long totalFileContentLength = 0;
			final long startAddress = XMemory.getDirectByteBufferAddress(fb);
			final long endAddress = startAddress + fb.limit();
			long currentAddress = startAddress;
				
			//read file header
			final FileHeader fileHeader = new FileHeader(startAddress);
			currentAddress += FileHeader.FILE_HEADER_SIZE;
						
			final CRC32 crc32 = new CRC32();
			fb.position(FileHeader.FILE_HEADER_SIZE);
			crc32.update(fb);
			fb.flip();

			if(fileHeader.contentCheckSum != crc32.getValue())
			{
				throw new RuntimeException("invalid checksum for indexFile: " + indexFile.identifier());
			}
			
			final List<StorageEntity.Default> tmpEntryList = new ArrayList<>((int)fileHeader.entityCount);
					
			while(currentAddress < endAddress)
			{
				final TypeInFileHeader typeInFileHeader = new TypeInFileHeader(currentAddress);
				currentAddress += TypeInFileHeader.TYPE_IN_FILE_HEADER_SIZE;
							
				entityCache.modifyUsedCacheSize(typeInFileHeader.entityCount);
												
				for(int entity = 0; entity < typeInFileHeader.entityCount; entity++)
				{
					final byte entityMetaData = XMemory.get_byte(currentAddress);
					currentAddress++;
					
					final int bytesOid     = (((entityMetaData & 0x07) )     ) +1;
					final int bytesFilePos = (((entityMetaData & 0x18) ) >> 3) +1;
					final int bytesLength  = (((entityMetaData & 0x60) ) >> 5) +1;
										
					long oidOffset = 0;
					int filePos = 0;
					int dataLength = 0;
										
					for(int i = 0; i <= bytesOid; i++)
					{
						oidOffset = oidOffset | ((XMemory.get_byte(currentAddress) & 0xFF) << i*8);
						currentAddress++;
					}
	
					for(int i = 0; i <= bytesFilePos; i++)
					{
						filePos = filePos | ((XMemory.get_byte(currentAddress) & 0xFF) << i*8);
						currentAddress++;
					}
			
					final long objectID = typeInFileHeader.baseOid + oidOffset;
										
					//only add first occurrence, others are outdated.
					if(entityCache.getEntry(objectID) != null)
					{
						logger.trace("Object {} already registered -> skip", objectID);
						
						if(typeInFileHeader.variableLength)
						{
							currentAddress += bytesLength+1;
						}
																		
						continue;
					}
					
					final StorageEntity.Default newEntity = entityCache.createEntity(objectID,  entityCache.getType(typeInFileHeader.typeId));
					
					
					if(typeInFileHeader.variableLength)
					{
						for(int i = 0; i <= bytesLength; i++)
						{
							dataLength = dataLength | ((XMemory.get_byte(currentAddress) & 0xFF) << i*8);
							currentAddress++;
						}
					}
					else
					{
						dataLength = (int) newEntity.typeInFile.type.typeHandler().maximumLength();
					}
					
										
					newEntity.updateStorageInformation(dataLength, filePos);
					
					tmpEntryList.add(newEntity);
					totalFileContentLength += dataLength;
				}
								
			}
			
			XMemory.deallocateDirectByteBuffer(fb);
			
			tmpEntryList.sort((o1, o2) -> Integer.compare(o1.storagePosition, o2.storagePosition));
			tmpEntryList.forEach(liveDataFile::appendEntry);
			
			liveDataFile.increaseContentLength(totalFileContentLength);
			liveDataFile.registerGapLength(liveDataFile.size() - totalFileContentLength);
		}


		@Override
		public boolean isUpToDate(final StorageLiveDataFile storageLiveDataFile, final StorageStartupIndexFile indexFile)
		{
			try
			{
				final ByteBuffer headerBuffer = XMemory.allocateDirectNative(FileHeader.FILE_HEADER_SIZE);
				indexFile.readBytes(headerBuffer, 0, FileHeader.FILE_HEADER_SIZE);
				final long bufferAddress = XMemory.getDirectByteBufferAddress(headerBuffer);
				final FileHeader fileHeader = new FileHeader(bufferAddress);
				
				if(storageLiveDataFile.dataLength() > 0)
				{
					if(fileHeader.liveDataLength != storageLiveDataFile.dataLength())
					{
						logger.debug("storage file {} does not matches index file{} (live data length mismatch)!",
							storageLiveDataFile.identifier(),
							indexFile.identifier());
						return false;
					}
				}
				else
				{
					if(storageLiveDataFile.file().size() != fileHeader.dataFileSize)
					{
						logger.debug("storage file {} does not matches index file{} (file size mismatch)!",
							storageLiveDataFile.identifier(),
							indexFile.identifier());
						return false;
					}
				}
				
			}
			catch (final Exception e) //does not really matter why. Index file is invalid.
			{
				logger.error("Failed to read index file header of file " + indexFile.identifier(), e);
				return false;
			}
			finally
			{
				indexFile.close();
			}
			
			return true;
		}

	}
	
}
