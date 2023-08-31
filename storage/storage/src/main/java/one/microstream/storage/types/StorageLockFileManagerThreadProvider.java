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

@FunctionalInterface
public interface StorageLockFileManagerThreadProvider extends StorageThreadProviding
{
	/**
	 * Provides a newly created, yet un-started {@link Thread} instance wrapping the passed
	 * {@link StorageLockFileManager} instance.
	 * The thread will be used as an exclusive, permanent lock file validator and updater worker thread
	 * until the storage is shut down.
	 * Interfering with the thread from outside the storage compound has undefined and potentially
	 * unpredictable and erroneous behavior.
	 * @param lockFileManager the lock file manager to wrap
	 * @return a {@link Thread} instance to be used as a storage lock file managing worker thread.
	 */
	public default Thread provideLockFileManagerThread(final StorageLockFileManager lockFileManager)
	{
		return this.provideLockFileManagerThread(lockFileManager, StorageThreadNameProvider.NoOp());
	}
	
	public Thread provideLockFileManagerThread(
		StorageLockFileManager    lockFileManager   ,
		StorageThreadNameProvider threadNameProvider
	);
	

	
	public static StorageLockFileManagerThreadProvider New()
	{
		return new StorageLockFileManagerThreadProvider.Default();
	}

	public final class Default implements StorageLockFileManagerThreadProvider
	{
		Default()
		{
			super();
		}
		
		@Override
		public Thread provideLockFileManagerThread(
			final StorageLockFileManager    lockFileManager   ,
			final StorageThreadNameProvider threadNameProvider
		)
		{
			final String threadName = StorageLockFileManager.class.getSimpleName();
			
			return new Thread(
				lockFileManager,
				threadNameProvider.provideThreadName(this, threadName)
			);
		}

	}

}
