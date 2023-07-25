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

public interface StorageHousekeepingBroker
{
	public boolean performIssuedFileCleanupCheck(
		StorageHousekeepingExecutor executor      ,
		long                        nanoTimeBudget
	);
	
	public boolean performIssuedGarbageCollection(
		StorageHousekeepingExecutor executor      ,
		long                        nanoTimeBudget
	);
	
	public boolean performIssuedEntityCacheCheck(
		StorageHousekeepingExecutor executor       ,
		long                        nanoTimeBudget ,
		StorageEntityCacheEvaluator entityEvaluator
	);
	
	
	public boolean performFileCleanupCheck(
		StorageHousekeepingExecutor executor      ,
		long                        nanoTimeBudget
	);
	
	public boolean performGarbageCollection(
		StorageHousekeepingExecutor executor      ,
		long                        nanoTimeBudget
	);
	
	public boolean performEntityCacheCheck(
		StorageHousekeepingExecutor executor      ,
		long                        nanoTimeBudget
	);
	
	public boolean performTransactionFileCheck(
		StorageHousekeepingExecutor executor ,
		boolean                     checkSize
	);
	
	public static StorageHousekeepingBroker New()
	{
		return new StorageHousekeepingBroker.Default();
	}
	
	public final class Default implements StorageHousekeepingBroker
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
		public boolean performIssuedFileCleanupCheck(
			final StorageHousekeepingExecutor executor      ,
			final long                        nanoTimeBudget
		)
		{
			return executor.performIssuedFileCleanupCheck(nanoTimeBudget);
		}
		
		@Override
		public boolean performIssuedGarbageCollection(
			final StorageHousekeepingExecutor executor      ,
			final long                        nanoTimeBudget
		)
		{
			return executor.performIssuedGarbageCollection(nanoTimeBudget);
		}
		
		@Override
		public boolean performIssuedEntityCacheCheck(
			final StorageHousekeepingExecutor executor      ,
			final long                        nanoTimeBudget,
			final StorageEntityCacheEvaluator evaluator
		)
		{
			return executor.performIssuedEntityCacheCheck(nanoTimeBudget, evaluator);
		}
		

		@Override
		public boolean performFileCleanupCheck(
			final StorageHousekeepingExecutor executor      ,
			final long                        nanoTimeBudget
		)
		{
			return executor.performFileCleanupCheck(nanoTimeBudget);
		}

		@Override
		public boolean performGarbageCollection(
			final StorageHousekeepingExecutor executor      ,
			final long                        nanoTimeBudget
		)
		{
			return executor.performGarbageCollection(nanoTimeBudget);
		}

		@Override
		public boolean performEntityCacheCheck(
			final StorageHousekeepingExecutor executor      ,
			final long                        nanoTimeBudget
		)
		{
			return executor.performEntityCacheCheck(nanoTimeBudget);
		}
		
		@Override
		public boolean performTransactionFileCheck(
			final StorageHousekeepingExecutor executor ,
			final boolean                     checkSize
		)
		{
			return executor.performTransactionFileCheck(checkSize);
		}
		
	}
	
}
