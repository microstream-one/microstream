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

import one.microstream.afs.types.AFile;

public interface StorageDataFile extends StorageChannelFile, StorageBackupableFile //, StorageClosableFile, StorageCreatableFile
{
	@Override
	public int channelIndex();
	
	public long number();
	
	
	
	@Override
	public default StorageBackupDataFile ensureBackupFile(final StorageBackupInventory backupInventory)
	{
		return backupInventory.ensureDataFile(this);
	}
	
	
	
	public static int orderByNumber(
		final StorageDataFile file1,
		final StorageDataFile file2
	)
	{
		return Long.compare(file1.number(), file2.number());
	}
	
	
	
	@FunctionalInterface
	public interface Creator<F extends StorageDataFile>
	{
		public F createDataFile(AFile file, int channelIndex, long number);
	}
	
	
	
	public abstract class Abstract extends StorageChannelFile.Abstract implements StorageDataFile
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final long number;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Abstract(final AFile file, final int channelIndex, final long number)
		{
			super(file, channelIndex);
			this.number = number;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final long number()
		{
			return this.number;
		}
		

	}
	
}
