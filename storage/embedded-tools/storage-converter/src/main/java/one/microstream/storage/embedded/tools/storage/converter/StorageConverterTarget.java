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

import java.io.Closeable;
import java.nio.ByteBuffer;

import org.slf4j.Logger;

import one.microstream.afs.types.AWritableFile;
import one.microstream.storage.types.StorageConfiguration;
import one.microstream.storage.types.StorageDataFileEvaluator;
import one.microstream.storage.types.StorageLiveFileProvider;
import one.microstream.util.logging.Logging;


/**
 * Handle everything related to the "Target Storage" of the storage converter.
 * 
 */
public class StorageConverterTarget implements Closeable
{
	private final static Logger logger = Logging.getLogger(StorageConverterTarget.class);
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final StorageLiveFileProvider      fileProvider;
	private final int                          channelCount;

	private final StorageConverterTargetFile[] files;
	private final StorageDataFileEvaluator     storageDataFileEvaluator;

	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
		
	/**
	 * Creates a {@link StorageConverterTarget} instance based on the supplied
	 * Storage configuration.
	 * 
	 * @param storageConfiguration {@link StorageConfiguration}
	 */
	public StorageConverterTarget(final StorageConfiguration storageConfiguration)
	{
		super();
		this.fileProvider             = storageConfiguration.fileProvider();
		this.channelCount             = storageConfiguration.channelCountProvider().getChannelCount();
		this.storageDataFileEvaluator = storageConfiguration.dataFileEvaluator();
		this.files                    = this.createTargetDataFiles();
	}

	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public void close()
	{
		for (final StorageConverterTargetFile file : this.files)
		{
			file.release();
		}
	}

	/**
	 * Write the supplied TypeDictionary String as the new targets TypeDictionary.
	 * 
	 * @param srcTypeDictionary the source type dictionary as String
	 */
	public void storeTypeDictionary(final String srcTypeDictionary)
	{
		logger.debug("Transferring type dictionary to target.");
		
		this.fileProvider.provideTypeDictionaryIoHandler().storeTypeDictionary(srcTypeDictionary);
	}
	
	/**
	 * Write the content of the supplied ByteBuffer
	 * to the targets' storage system.
	 * 
	 * @param buffer content to be transferred
	 * @param oid object id
	 */
	public void transferBytes(final ByteBuffer buffer, final long oid)
	{
		final int targetChannel = (int) (oid % this.channelCount);
		
		logger.trace("Transferring blob pos: {}, limit: {} to target channel {}.",
				buffer.position(), buffer.limit(), targetChannel);
		
		this.transferBytesToChannel(buffer, targetChannel);
	}

	private void transferBytesToChannel(final ByteBuffer buffer, final int channelIndex)
	{
		StorageConverterTargetFile file = this.files[channelIndex];

		if (buffer.limit() - buffer.position() + file.size() > this.storageDataFileEvaluator.fileMaximumSize())
		{
			file.release();
			file = this.createNewStorageFile(file.fileNumber() + 1, channelIndex);
			this.files[channelIndex] = file;
		}

		this.files[channelIndex].writeBytes(buffer);
	}

	private StorageConverterTargetFile[] createTargetDataFiles()
	{
		final StorageConverterTargetFile[] files = new StorageConverterTargetFile[this.channelCount];
		
		for (int channelIndex = 0; channelIndex < this.channelCount; channelIndex++)
		{
			files[channelIndex] = this.createNewStorageFile(0, channelIndex);
		}
		
		return files;
	}

	private StorageConverterTargetFile createNewStorageFile(final long fileNumber, final int channelIndex)
	{
		logger.debug("Creating new storage file {} for channel {}.", fileNumber, channelIndex);
		
		final AWritableFile file = this.fileProvider.provideDataFile(channelIndex, fileNumber).useWriting();
		file.ensureExists();
		
		return new StorageConverterTargetFile(file, fileNumber);
	}
}
