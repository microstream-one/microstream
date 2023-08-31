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
public interface StorageBackupThreadProvider extends StorageThreadProviding
{
	/**
	 * Provides a newly created, yet un-started {@link Thread} instance wrapping the passed
	 * {@link StorageBackupHandler} instance.
	 * The thread will be used as an exclusive, permanent backup worker thread until the storage
	 * is shut down.
	 * Interfering with the thread from outside the storage compound has undefined and potentially
	 * unpredictable and erronous behavior.
	 *
	 * @param backupHandler the handler to wrap
	 * @return a {@link Thread} instance to be used as a storage backup worker thread.
	 */
	public default Thread provideBackupThread(final StorageBackupHandler backupHandler)
	{
		return this.provideBackupThread(backupHandler, StorageThreadNameProvider.NoOp());
	}
	
	public Thread provideBackupThread(
		StorageBackupHandler      backupHandler     ,
		StorageThreadNameProvider threadNameProvider
	);

	
	
	public static StorageBackupThreadProvider New()
	{
		return new StorageBackupThreadProvider.Default();
	}

	public final class Default implements StorageBackupThreadProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public Thread provideBackupThread(
			final StorageBackupHandler      backupHandler     ,
			final StorageThreadNameProvider threadNameProvider
		)
		{
			final String threadName = StorageBackupHandler.class.getSimpleName();
			
			return new Thread(
				backupHandler,
				threadNameProvider.provideThreadName(this, threadName)
			);
		}

	}

}
