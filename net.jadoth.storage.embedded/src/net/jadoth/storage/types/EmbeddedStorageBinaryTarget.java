package net.jadoth.storage.types;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.exceptions.PersistenceExceptionTransfer;
import net.jadoth.persistence.types.PersistenceTarget;

public interface EmbeddedStorageBinaryTarget extends PersistenceTarget<Binary>
{
	@Override
	public void write(Binary[] data) throws PersistenceExceptionTransfer;



	public final class Implementation implements EmbeddedStorageBinaryTarget
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final StorageRequestAcceptor requestAcceptor;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(final StorageRequestAcceptor requestAcceptor)
		{
			super();
			this.requestAcceptor = requestAcceptor;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final void write(final Binary[] data) throws PersistenceExceptionTransfer
		{
			try
			{
				this.requestAcceptor.storeData(data);
			}
			catch(final Exception e)
			{
				throw new PersistenceExceptionTransfer(e);
			}
		}

	}

}
