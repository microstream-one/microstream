package net.jadoth.network.persistence;

import java.nio.channels.SocketChannel;

import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.persistence.exceptions.PersistenceExceptionTransfer;
import net.jadoth.persistence.types.PersistenceChannel;
import net.jadoth.swizzling.types.SwizzleIdSet;


public interface ComPersistenceChannel<M> extends PersistenceChannel<M>
{
	
	public abstract class AbstractImplementation<M> implements ComPersistenceChannel<M>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final SocketChannel channel;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		protected AbstractImplementation(final SocketChannel channel)
		{
			super();
			this.channel = channel;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		protected abstract XGettingCollection<? extends M> readFromSocketChannel(SocketChannel channel)
			 throws PersistenceExceptionTransfer;
		
		protected abstract void writeToSocketChannel(SocketChannel channel, M[] data)
			 throws PersistenceExceptionTransfer;

		@Override
		public XGettingCollection<? extends M> read() throws PersistenceExceptionTransfer
		{
			return this.readFromSocketChannel(this.channel);
		}

		@Override
		public void write(final M[] data) throws PersistenceExceptionTransfer
		{
			this.writeToSocketChannel(this.channel, data);
		}
		
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
			
			// FIXME NetworkPersistenceConnection<M>#readByObjectIds()
			throw new net.jadoth.meta.NotImplementedYetError();
		}
		
		// (08.08.2018 TM)FIXME: prepare and close implementations
		
	}
	
}
