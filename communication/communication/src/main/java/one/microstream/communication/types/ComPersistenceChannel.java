package one.microstream.communication.types;

/*-
 * #%L
 * microstream-communication
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

import one.microstream.collections.types.XGettingCollection;
import one.microstream.persistence.exceptions.PersistenceExceptionTransfer;
import one.microstream.persistence.types.PersistenceChannel;
import one.microstream.persistence.types.PersistenceIdSet;

/**
 * 
 * @param <C> the communication layer type
 * @param <D> the data type
 */
public interface ComPersistenceChannel<C, D> extends PersistenceChannel<D>
{
	
	public abstract class Abstract<C, D> implements ComPersistenceChannel<C, D>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final C connection;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected Abstract(final C connection)
		{
			super();
			this.connection = connection;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		protected final C getConnection()
		{
			return this.connection;
		}
		
		protected abstract XGettingCollection<? extends D> internalRead(C connection)
			 throws PersistenceExceptionTransfer;
		
		protected abstract void internalWrite(C channel, D data)
			 throws PersistenceExceptionTransfer;

		@Override
		public XGettingCollection<? extends D> read() throws PersistenceExceptionTransfer
		{
			return this.internalRead(this.connection);
		}

		@Override
		public void write(final D data) throws PersistenceExceptionTransfer
		{
			this.internalWrite(this.connection, data);
		}
		
		@Override
		public synchronized void prepareChannel()
		{
			this.prepareSource();
			this.prepareTarget();
		}
		
		@Override
		public synchronized void closeChannel()
		{
			this.closeTarget();
			this.closeSource();
		}
		
		@Override
		public abstract void prepareSource();
		
		@Override
		public abstract void prepareTarget();
		
		@Override
		public abstract void closeSource();
		
		@Override
		public abstract void closeTarget();
		
		@Override
		public XGettingCollection<? extends D> readByObjectIds(final PersistenceIdSet[] objectIds)
			throws PersistenceExceptionTransfer
		{
			/* (08.08.2018 TM)NOTE:
			 * Makes sense in principle. One side of a network connection requests data specified by a set of OIDs.
			 * That also means that every batch of sent data must be inherently complete, i.e. may never trigger this
			 * method to request missing data for unresolvable OIDs.
			 * 
			 * However, such a function is not supported for the current simple proof-of-concept.
			 */
			
			// TODO NetworkPersistenceConnection<D>#readByObjectIds()
			throw new one.microstream.meta.NotImplementedYetError();
		}
		
	}
	
}
