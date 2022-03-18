package one.microstream.persistence.types;

/*-
 * #%L
 * microstream-persistence
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
import one.microstream.persistence.internal.PersistenceTypeDictionaryFileHandler;

public interface PersistenceTypeDictionaryIoHandler
extends PersistenceTypeDictionaryLoader, PersistenceTypeDictionaryStorer
{
	// just a typing interface so far
		
	public interface Provider
	{
		public default PersistenceTypeDictionaryIoHandler provideTypeDictionaryIoHandler()
		{
			return this.provideTypeDictionaryIoHandler(null);
		}
		
		public PersistenceTypeDictionaryIoHandler provideTypeDictionaryIoHandler(
			PersistenceTypeDictionaryStorer writeListener
		);
		
		
		public abstract class Abstract implements PersistenceTypeDictionaryIoHandler.Provider
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			private final PersistenceTypeDictionaryFileHandler.Creator fileHandlerCreator;
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			protected Abstract(final PersistenceTypeDictionaryFileHandler.Creator fileHandlerCreator)
			{
				super();
				this.fileHandlerCreator = fileHandlerCreator;
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////

			protected abstract AFile defineTypeDictionaryFile();

			@Override
			public PersistenceTypeDictionaryIoHandler provideTypeDictionaryIoHandler(
				final PersistenceTypeDictionaryStorer writeListener
			)
			{
				/*
				 * (04.03.2019 TM)TODO: forced delegating API is not a clean solution.
				 * This is only a temporary solution. See the task containing "PersistenceDataFile".
				 */
				final AFile file = this.defineTypeDictionaryFile();
				
				return this.fileHandlerCreator.createTypeDictionaryIoHandler(file, writeListener);
			}
			
		}
		
	}
		
}
