package one.microstream.storage.types;

/*-
 * #%L
 * microstream-storage
 * %%
 * Copyright (C) 2019 - 2021 MicroStream Software
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

public interface StorageThreadProvider
extends StorageChannelThreadProvider, StorageBackupThreadProvider, StorageLockFileManagerThreadProvider
{
	public static StorageThreadProvider New(
		final StorageChannelThreadProvider         channelThreadProvider        ,
		final StorageBackupThreadProvider          backupThreadProvider         ,
		final StorageLockFileManagerThreadProvider lockFileManagerThreadProvider
	)
	{
		return New(
			StorageThreadNameProvider.NoOp(),
			channelThreadProvider           ,
			backupThreadProvider            ,
			lockFileManagerThreadProvider
		);
	}
	
	public static StorageThreadProvider New(
		final StorageThreadNameProvider            threadNameProvider           ,
		final StorageChannelThreadProvider         channelThreadProvider        ,
		final StorageBackupThreadProvider          backupThreadProvider         ,
		final StorageLockFileManagerThreadProvider lockFileManagerThreadProvider
	)
	{
		return new StorageThreadProvider.Wrapper(
			notNull(threadNameProvider)           ,
			notNull(channelThreadProvider)        ,
			notNull(backupThreadProvider)         ,
			notNull(lockFileManagerThreadProvider)
		);
	}

	public final class Wrapper implements StorageThreadProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final StorageThreadNameProvider            threadNameProvider           ;
		private final StorageChannelThreadProvider         channelThreadProvider        ;
		private final StorageBackupThreadProvider          backupThreadProvider         ;
		private final StorageLockFileManagerThreadProvider lockFileManagerThreadProvider;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Wrapper(
			final StorageThreadNameProvider            threadNameProvider           ,
			final StorageChannelThreadProvider         channelThreadProvider        ,
			final StorageBackupThreadProvider          backupThreadProvider         ,
			final StorageLockFileManagerThreadProvider lockFileManagerThreadProvider
		)
		{
			super();
			this.threadNameProvider            = threadNameProvider           ;
			this.channelThreadProvider         = channelThreadProvider        ;
			this.backupThreadProvider          = backupThreadProvider         ;
			this.lockFileManagerThreadProvider = lockFileManagerThreadProvider;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final Thread provideChannelThread(final StorageChannel storageChannel)
		{
			return this.channelThreadProvider.provideChannelThread(
				storageChannel,
				this.threadNameProvider
			);
		}

		@Override
		public final Thread provideBackupThread(final StorageBackupHandler backupHandler)
		{
			return this.backupThreadProvider.provideBackupThread(
				backupHandler,
				this.threadNameProvider
			);
		}
		
		@Override
		public final Thread provideLockFileManagerThread(final StorageLockFileManager lockFileManager)
		{
			return this.lockFileManagerThreadProvider.provideLockFileManagerThread(
				lockFileManager,
				this.threadNameProvider
			);
		}

		@Override
		public final Thread provideChannelThread(
			final StorageChannel            storageChannel    ,
			final StorageThreadNameProvider threadNameProvider
		)
		{
			return this.channelThreadProvider.provideChannelThread(
				storageChannel,
				threadNameProvider
			);
		}

		@Override
		public final Thread provideBackupThread(
			final StorageBackupHandler      backupHandler     ,
			final StorageThreadNameProvider threadNameProvider
		)
		{
			return this.backupThreadProvider.provideBackupThread(
				backupHandler,
				threadNameProvider
			);
		}

		@Override
		public final Thread provideLockFileManagerThread(
			final StorageLockFileManager    lockFileManager   ,
			final StorageThreadNameProvider threadNameProvider
		)
		{
			return this.lockFileManagerThreadProvider.provideLockFileManagerThread(
				lockFileManager,
				threadNameProvider
			);
		}

	}

}
