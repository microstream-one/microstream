package net.jadoth.com;

import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.persistence.exceptions.PersistenceExceptionTransfer;
import net.jadoth.persistence.types.PersistenceChannel;
import net.jadoth.swizzling.types.SwizzleIdSet;


public interface ComPersistenceChannel<C, M> extends PersistenceChannel<M>
{
	
	public abstract class AbstractImplementation<C, M> implements ComPersistenceChannel<C, M>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final C connection;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected AbstractImplementation(final C connection)
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
		
		protected abstract XGettingCollection<? extends M> internalRead(C connection)
			 throws PersistenceExceptionTransfer;
		
		protected abstract void internalWrite(C channel, M[] data)
			 throws PersistenceExceptionTransfer;

		@Override
		public XGettingCollection<? extends M> read() throws PersistenceExceptionTransfer
		{
			return this.internalRead(this.connection);
		}

		@Override
		public void write(final M[] data) throws PersistenceExceptionTransfer
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
		public XGettingCollection<? extends M> readByObjectIds(final SwizzleIdSet[] oids) throws PersistenceExceptionTransfer
		{
			/* (08.08.2018 TM)NOTE:
			 * Makes sense in principle. One side of a network connection requests data specified by a set of OIDs.
			 * That also means that every batch of sent data must be inherently complete, i.e. may never trigger this
			 * method to request missing data for unresolvable OIDs.
			 * 
			 * However, such a function is not supported for the current simple proof-of-concept.
			 */
			
			// TODO NetworkPersistenceConnection<M>#readByObjectIds()
			throw new net.jadoth.meta.NotImplementedYetError();
		}
		
	}
	
}
