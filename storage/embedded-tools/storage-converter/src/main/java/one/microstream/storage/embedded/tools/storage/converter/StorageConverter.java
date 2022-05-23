package one.microstream.storage.embedded.tools.storage.converter;

/*-
 * #%L
 * MicroStream Embedded Storage Tools Converter
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

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.slf4j.Logger;

import one.microstream.collections.EqHashTable;
import one.microstream.collections.XSort;
import one.microstream.collections.types.XGettingList;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.storage.exceptions.StorageExceptionConsistency;
import one.microstream.storage.types.StorageConfiguration;
import one.microstream.storage.types.StorageDataInventoryFile;
import one.microstream.storage.types.StorageInventory;
import one.microstream.storage.types.StorageLiveFileProvider;
import one.microstream.util.logging.Logging;

/**
 * 
 * Converts a EmbeddedStorage into another one.
 *
 */
public class StorageConverter
{
	private final static Logger logger = Logging.getLogger(StorageConverter.class);

	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final StorageLiveFileProvider   srcFileProvider;
	private final int                       srcChannelCount;
	private final StorageInventory[]        srcInventories;

	private final HashSet<Long>             processedIds;
	private final HashMap<Long, FileEntity> currentFileEntities;

	private       ByteBuffer                bufferIn;
	private final StorageConverterTarget    target;

	/**
	 * Helper class describing a single entity in the current processed file
	 */
	private static class FileEntity
	{
		final long offset;
		final long length;

		public FileEntity(final long offset, final long length)
		{
			super();
			this.offset = offset;
			this.length = length;
		}
	}

	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	/**
	 * Converts a EmbeddedStorage into another one.
	 * 
	 * @param sourceStorageConfiguration configuration for storage to be converted.
	 * @param targetStorageConfiguration configuration of the target storage.
	 */
	public StorageConverter	(
		final StorageConfiguration sourceStorageConfiguration,
		final StorageConfiguration targetStorageConfiguration
	)
	{
		this.srcFileProvider     = sourceStorageConfiguration.fileProvider();
		this.srcChannelCount     = sourceStorageConfiguration.channelCountProvider().getChannelCount();

		this.srcInventories      = this.createChannelInventories();
		this.processedIds        = new HashSet<>();
		this.currentFileEntities = new HashMap<>();

		this.target = new StorageConverterTarget(targetStorageConfiguration);
	}
	

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	/**
	 * Execute the conversion.
	 */
	public void start()
	{
		this.copyTypeDictionary();
		this.convertStorage();
		this.close();
	}

	private void copyTypeDictionary()
	{
		this.target
			.storeTypeDictionary(this.srcFileProvider.provideTypeDictionaryIoHandler().loadTypeDictionary());
	}

	private void close()
	{
		this.target.close();
	}

	private void transferEntity(final long oid, final FileEntity entity)
	{
		logger.trace("Processing entity {}.", oid);

		this.bufferIn.limit((int) (entity.offset + entity.length));
		this.bufferIn.position((int) entity.offset);

		this.target.transferBytes(this.bufferIn, oid);
		this.processedIds.add(oid);
	}

	private void processFile(final StorageDataInventoryFile storageDataInventoryFile)
	{
		logger.debug("Processing storageFile: {}", storageDataInventoryFile.identifier());

		this.bufferIn = XMemory.allocateDirectNative(storageDataInventoryFile.size());
		storageDataInventoryFile.readBytes(this.bufferIn);

		final long bufferStartAddress = XMemory.getDirectByteBufferAddress(this.bufferIn);
		final long bufferBoundAddress = bufferStartAddress + this.bufferIn.limit();

		long currentItemLength;
		long offset = 0;

		for (long address = bufferStartAddress; address < bufferBoundAddress;)
		{
			currentItemLength = Binary.getEntityLengthRawValue(address);

			if (currentItemLength > 0)
			{
				this.registerFileEntity(address, offset, currentItemLength);

				address += currentItemLength;
				offset += currentItemLength;
			}
			else if (currentItemLength < 0)
			{
				// comments (indicated by negative length) just get skipped.
				// note that gap length gets registered for the file at the end arithmetically
				address -= currentItemLength;
				offset -= currentItemLength;
			}
			else
			{
				// entity length may never be 0 or the iteration will hang forever
				throw new StorageExceptionConsistency("Zero length data item.");
			}
		}

		this.transferRegisteredEntities();

		logger.trace("Clearing current file entities.");
		this.currentFileEntities.clear();
	}

	private void transferRegisteredEntities()
	{
		this.currentFileEntities.forEach(this::transferRegisteredEntity);
	}

	private void transferRegisteredEntity(final long oid, final FileEntity fileEntry)
	{
		this.transferEntity(oid, fileEntry);
	}

	private void registerFileEntity(final long address, final long offset, final long itemLength)
	{
		final long oid = Binary.getEntityObjectIdRawValue(address);

		if (this.processedIds.contains(oid))
		{
			logger.trace("Oid {} skipped, already processed.", oid);
			return;
		}

		if (this.currentFileEntities.put(oid, new FileEntity(offset, itemLength)) == null)
		{
			logger.trace("Adding new FileEntry oid {}, address {}, offset {}, length {} for processing.",
					oid, address, offset, itemLength);
		}
		else
		{
			logger.trace("Replaced existing FileEntry for oid {}, address {}, offset {}, length {} for processing.",
					oid, address, offset, itemLength);
		}
	}

	private void convertStorage()
	{
		for (final StorageInventory channelInventory : this.srcInventories)
		{
			this.processChannel(channelInventory);
		}
	}

	private void processChannel(final StorageInventory channelInventory)
	{
		logger.trace("Processing channel {}.", channelInventory.channelIndex());

		final XGettingList<StorageDataInventoryFile> reversedFiles = channelInventory.dataFiles().values().toReversed();
		final Iterator<? extends StorageDataInventoryFile> iterator = reversedFiles.iterator();

		while (iterator.hasNext())
		{
			this.processFile(iterator.next());
		}
	}

	private StorageInventory[] createChannelInventories()
	{
		final StorageInventory[] inventories = new StorageInventory[this.srcChannelCount];
		for (int i = 0; i < this.srcChannelCount; i++)
		{
			inventories[i] = this.createChannelInventory(i);
		}
		return inventories;
	}

	private StorageInventory createChannelInventory(final int channelIndex)
	{
		final EqHashTable<Long, StorageDataInventoryFile> dataFiles = EqHashTable.New();
		this.srcFileProvider.collectDataFiles(StorageDataInventoryFile::New, f -> dataFiles.add(f.number(), f),
				channelIndex);
		dataFiles.keys().sort(XSort::compare);

		return StorageInventory.New(channelIndex, dataFiles, null);
	}

}
