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

import one.microstream.storage.exceptions.StorageExceptionShutdown;

public interface StorageController extends StorageActivePart, AutoCloseable
{
	/**
	 * "Starts" the storage controlled by this {@link StorageController} instance, with "starting" meaning:<br>
	 * <ul>
	 * <li>Reading, indexing and potentially caching all the persisted data in the storage.</li>
	 * <li>Starting storage managing threads to execute requests like storing, loading and issued utility functions.</li>
	 * </ul>
	 * 
	 * @return this to allow writing of fluent code.
	 */
	public StorageController start();

	/**
	 * Issues a command to shut down all active threads managing the storage.
	 * 
	 * @return <code>true</code> after a successful shutdown or <code>false</code>
	 *         if an internal {@link InterruptedException} happened.
	 */
	public boolean shutdown();

	public boolean isAcceptingTasks();

	public boolean isRunning();

	public boolean isStartingUp();

	public boolean isShuttingDown();

	public default boolean isShutdown()
	{
		return !this.isRunning();
	}

	public void checkAcceptingTasks();
	
	public long initializationTime();
	
	public long operationModeTime();
	
	public default long initializationDuration()
	{
		return this.operationModeTime() - this.initializationTime();
	}
	
	@Override
	public default void close() throws StorageExceptionShutdown
	{
		boolean success;
		try
		{
			success = this.shutdown();
		}
		catch(final Exception e)
		{
			throw new StorageExceptionShutdown("Shutdown failed.", e);
		}
		
		if(!success)
		{
			throw new StorageExceptionShutdown("Shutdown failed.");
		}
	}

}
