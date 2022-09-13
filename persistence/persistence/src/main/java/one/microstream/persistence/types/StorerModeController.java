package one.microstream.persistence.types;

/*-
 * #%L
 * MicroStream Persistence
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

import java.lang.ref.WeakReference;

import org.slf4j.Logger;

import one.microstream.collections.BulkList;
import one.microstream.collections.types.XList;
import one.microstream.util.logging.Logging;

/**
 * The StorerModeController provides API to control the write mode of
 * registered PersistenceStorer implementations from a central instance.
 * <br>
 * Storers are register with {@link #register(PersistenceStorerDeactivateAble)}.
 * They will be registers using {@code WeakReferences}. That will be checked and
 * cleaned up periodically.
 * <br>
 * <br>
 * As this class starts in internal thread use the {@link #shutdown()} call to speed up
 * termination. Otherwise the internal thread will run until all registered stores
 * are disposed by the JVMs garbage collector.
 * <br>
 * 
 * Usage:
 * <pre>{@code
final StorerModeController storerModeController = new StorerModeController();
final EmbeddedStorageManager storage = EmbeddedStorage
	.Foundation()
	.onConnectionFoundation(connectionFoundation ->
		connectionFoundation.setStorerCreator(
			PersistenceStorerCreatorDeactivateAble.New(
				connectionFoundation,
				storerModeController)))
	.start();

storerModeController.disableWriting();

assertThrows(PersistenceExceptionStorerDeactivated.class,
	() -> storage.store("Hello World"));
 * }</pre>
 * 
 */
public class StorerModeController
{
	private final static Logger logger = Logging.getLogger(StorerModeController.class);
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private XList<WeakReference<PersistenceStorerDeactivateAble>> registry;
	private boolean                                               enabledWrites = true;
	private CleaningThread                                        cleaningThread;
	private boolean                                               active;

	/**
	 * A helper thread that iterates all registered storers and removes them if
	 * garbage collected.
	 */
	private static class CleaningThread extends Thread
	{
		
		StorerModeController controller;
		
		public CleaningThread(final StorerModeController controller)
		{
			super("Microstream-StorerModeController");
			this.controller = controller;
		}
		
		@Override
		public void run()
		{
			while(this.controller.hasRegisteredStorers() && this.controller.isActive())
			{
				try
				{
					Thread.sleep(1000);
				}
				catch (final InterruptedException e)
				{
					//no need to end thread.
					logger.debug("Ignored an InterruptedException while sleeping!");
				}
				this.controller.clean();
			}
		}
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public StorerModeController()
	{
		super();
		this.registry = BulkList.New();
		this.active = true;
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	/**
	 * Returns the state of this StorerModeController.
	 * 
	 * @return true if not shut down.
	 */
	public synchronized boolean isActive()
	{
		return this.active;
	}

	/**
	 * Shutdown this StorerModeController.
	 */
	public synchronized void shutdown()
	{
		this.active = false;
	}
	
	/**
	 * Register a PersistenceStorerDeactivateAble instance to the StorerModeController.
	 * 
	 * @param storer the PersistenceStorerDeactivateAble to be registered.
	 * @return the registered instance.
	 */
	public synchronized PersistenceStorerDeactivateAble register(final PersistenceStorerDeactivateAble storer)
	{
		this.registry.add(new WeakReference<>(storer));
		storer.enableWrites(this.enabledWrites);
		this.startCleaningTask();
		return storer;
	}
	
	/**
	 * Returns true if there are any registered PersistenceStorerDeactivateAble instances.
	 * 
	 * @return true or false.
	 */
	public synchronized boolean hasRegisteredStorers()
	{
		return this.registry.size() > 0;
	}
		
	/**
	 * Cleanup all no more valid (garbage collected) Storer instances.
	 */
	public synchronized void clean()
	{
		final XList<WeakReference<PersistenceStorerDeactivateAble>> newRegistry = BulkList.New();
		
		this.registry.forEach( e ->
		{
			final Object referent = e.get();
			if(referent!=null)
			{
				newRegistry.add(e);
			}
		});
		
		this.registry = newRegistry;
		logger.trace("Active storers after cleanup: {}", this.registry.size());
	}
	
	/**
	 * Switch all registered storers to the writing mode.
	 * 
	 * @return true if writing is enabled, otherwise false.
	 */
	public synchronized boolean enableWriting()
	{
		this.enabledWrites  = true;
		this.enableAll();
		return this.enabledWrites;
	}
	
	/**
	 * Switch all registered storers to the read only mode.
	 * 
	 * @return true if writing is disabled, otherwise false.
	 */
	public synchronized boolean disableWriting()
	{
		this.enabledWrites = false;
		this.disableAll();
		return !this.enabledWrites;
	}
	
	/**
	 * Gets the current state if writing is allowed or not.
	 * 
	 * @return true if writing is allowed, otherwise false.
	 */
	public synchronized boolean enabled()
	{
		return this.enabledWrites;
	}
		
	/**
	 * Start the internal thread that periodically checks for deceased
	 * storers.
	 */
	private void startCleaningTask()
	{
		if(this.cleaningThread == null || !this.cleaningThread.isAlive())
		{
			logger.debug("Starting new cleaning task");
			this.cleaningThread = new CleaningThread(this);
			this.cleaningThread.start();
		}
	}
	
	/**
	 * Iterate all registered storers and enable the write mode.
	 */
	private void enableAll()
	{
		this.registry.forEach( r ->
		{
			final PersistenceStorerDeactivateAble storer = r.get();
			if(storer != null) {
				storer.enableWrites();
			}
		});
	}
	
	/**
	 * Iterate all registered storers and disable the write mode.
	 */
	private void disableAll()
	{
		this.registry.forEach( r ->
		{
			final PersistenceStorerDeactivateAble storer = r.get();
			if(storer != null) {
				storer.disableWrites();
			}
		});
	}
}
