package net.jadoth.network.persistence;

import java.nio.channels.SocketChannel;

import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.persistence.exceptions.PersistenceExceptionTransfer;
import net.jadoth.persistence.types.PersistenceChannel;
import net.jadoth.swizzling.types.SwizzleIdSet;

public interface NetworkPersistenceChannel<M> extends PersistenceChannel<M>
{
	
	public abstract class AbstractImplementation<M> implements NetworkPersistenceChannel<M>
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
			 * However, for a simple initial demo, such a function is not supported.
			 * That also means that every batch of sent data must be inherently complete, i.e. may never trigger this
			 * method to request missing data for unresolvable OIDs.
			 */
			
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME NetworkPersistenceConnection<M>#readByObjectIds()
		}
		
		// (08.08.2018 TM)FIXME: prepare and close implementations
		
	}
	
}
