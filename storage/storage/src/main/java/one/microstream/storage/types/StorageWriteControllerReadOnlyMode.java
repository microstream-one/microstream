package one.microstream.storage.types;

/*-
 * #%L
 * MicroStream Storage
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

import org.slf4j.Logger;

import one.microstream.afs.types.WriteController;
import one.microstream.util.logging.Logging;

/**
 * {@link StorageWriteController} implementation that
 * allows to switch between the supplied and a read only
 * StorageWriteController. If the read only mode is disabled
 * the supplied StorageWriteController will be used.
 * 
 * <br><br>
 * Setup:
 * 
 * <pre> {@code
 *
 * final EmbeddedStorageFoundation<?> foundation = EmbeddedStorage.Foundation();
 *
 * final StorageWriteControllerReadOnlyMode storageWriteController =
 *	new StorageWriteControllerReadOnlyMode(foundation.getWriteController());
 *
 * foundation.setWriteController(storageWriteController);
 *
 * final EmbeddedStorageManager storage = foundation.start();
 * }
 * </pre>
 *
 */
public class StorageWriteControllerReadOnlyMode implements StorageWriteController
{
	private final static Logger logger = Logging.getLogger(StorageWriteController.class);
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private WriteController writeController;
	private final WriteController defaultController;
	private final WriteController readOnlyController;

	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	/**
	 * Create a new instance of a StorageWriteControllerReadOnlyMode.
	 * The constructed instance will have the read only mode ENABLED.
	 * 
	 * @param writeController the {@link StorageWriteController} instance
	 * that is used if the read only mode is disabled.
	 */
	public StorageWriteControllerReadOnlyMode(final WriteController writeController)
	{
		super();
		this.defaultController = writeController;
		this.readOnlyController = WriteController.Disabled();
		this.writeController = this.readOnlyController;
		
		logger.info("Created StorageWriteControllerReadOnlyMode with Storage read-only mode enabled: {}!",
			this.writeController == this.readOnlyController
		);
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public void setReadOnly(final boolean readOnly)
	{
		if(readOnly)
		{
			this.writeController = this.readOnlyController;
		}
		else
		{
			this.writeController = this.defaultController;
		}
		
		logger.info("Read-only mode enabled: {}", this.writeController == this.readOnlyController);
	}
	
	public boolean isReadOnly()
	{
		return this.writeController == this.readOnlyController;
	}
	
	@Override
	public void validateIsWritable()
	{
		this.writeController.validateIsWritable();
	}

	@Override
	public boolean isWritable()
	{
		return this.writeController.isWritable();
	}

	@Override
	public boolean isFileCleanupEnabled()
	{
		return this.writeController.isWritable();
	}

	@Override
	public boolean isBackupEnabled()
	{
		//must return true to start the backup handler even if writing
		//is not enabled during startup!
		return this.defaultController.isWritable();
	}

	@Override
	public boolean isDeletionDirectoryEnabled()
	{
		return this.writeController.isWritable();
	}

	@Override
	public boolean isFileDeletionEnabled()
	{
		return this.writeController.isWritable();
	}

}
