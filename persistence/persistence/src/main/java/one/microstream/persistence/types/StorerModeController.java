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
 * Storers are register with {@link #register(PersistenceStorerDeactivatable)}.
 * They will be registers using {@code WeakReferences}. That will be checked and
 * cleaned up periodically.
 * <br>
 * <br>
 * As this class starts an internal thread use the {@link #shutdown()} call to speed up
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
			PersistenceStorerCreatorDeactivatable.New(
				connectionFoundation,
				storerModeController)))
	.start();

storerModeController.disableWriting();

assertThrows(PersistenceExceptionStorerDeactivated.class,
	() -> storage.store("Hello World"));
 * }</pre>
 * 
 */
public class StorerModeController implements PersistenceStorerDeactivatableRegistry
{
	private final static Logger logger = Logging.getLogger(StorerModeController.class);
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private XList<WeakReference<PersistenceStorerDeactivatable>> registry;
	private boolean                                              enabledWrites = true;
	private CleaningThread                                       cleaningThread;
	private boolean                                              active;

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
	 * Enable or disable writing support for all registered storers.
	 * 
	 * @param enableWrites true to enable writing, false to disable.
	 */
	public void setWriteEnabled(final boolean enableWrites)
	{
		if(enableWrites)
		{
			this.enableWrites();
		}
		else
		{
			this.disableWrites();
		}
	}
	
	/**
	 * Gets the current state if writing is allowed or not.
	 * 
	 * @return true if writing is allowed, otherwise false.
	 */
	public synchronized boolean isWriteEnabled()
	{
		return this.enabledWrites;
	}
	
	/**
	 * Switch all registered storers to the writing mode.
	 * 
	 */
	public synchronized void enableWrites()
	{
		this.enabledWrites  = true;
		this.enableAll();
	}
	
	/**
	 * Switch all registered storers to the read only mode.
	 * 
	 */
	public synchronized void disableWrites()
	{
		this.enabledWrites = false;
		this.disableAll();
	}
		
	@Override
	public synchronized PersistenceStorerDeactivatable register(final PersistenceStorerDeactivatable deactivatableStorer)
	{
		this.registry.add(new WeakReference<>(deactivatableStorer));
		deactivatableStorer.setWriteEnabled(this.enabledWrites);
		this.startCleaningTask();
		return deactivatableStorer;
	}
	
	@Override
	public synchronized boolean hasRegisteredStorers()
	{
		return this.registry.size() > 0;
	}
		
	@Override
	public synchronized void clean()
	{
		final XList<WeakReference<PersistenceStorerDeactivatable>> newRegistry = BulkList.New();
		
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
			final PersistenceStorerDeactivatable storer = r.get();
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
			final PersistenceStorerDeactivatable storer = r.get();
			if(storer != null) {
				storer.disableWrites();
			}
		});
	}
}
