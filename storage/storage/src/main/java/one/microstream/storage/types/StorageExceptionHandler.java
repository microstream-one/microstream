package one.microstream.storage.types;

import org.slf4j.Logger;

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

import one.microstream.storage.exceptions.StorageException;
import one.microstream.util.logging.Logging;

@FunctionalInterface
public interface StorageExceptionHandler
{
	public void handleException(Throwable exception, StorageChannel channel);
	
	
	
	public static void defaultHandleException(final Throwable exception, final StorageChannel channel)
	{
		// logic encapsulated in static method to be reusable by other implementors.
		if(exception instanceof StorageException)
		{
			throw (StorageException)exception;
		}
		throw new StorageException(exception);
	}
	
	
	
	public static StorageExceptionHandler New()
	{
		return new StorageExceptionHandler.Default();
	}
	
	public final class Default implements StorageExceptionHandler
	{
		private final static Logger logger = Logging.getLogger(Default.class);
		
		Default()
		{
			super();
		}
		
		@Override
		public void handleException(final Throwable exception, final StorageChannel channel)
		{
			logger.error("Exception occurred in storage channel#{}", channel.channelIndex(), exception);
			
			StorageExceptionHandler.defaultHandleException(exception, channel);
		}
		
	}
	
}
