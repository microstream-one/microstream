package one.microstream.storage.types;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.exceptions.PersistenceExceptionTransfer;
import one.microstream.persistence.types.PersistenceTarget;
import one.microstream.storage.types.StorageRequestAcceptor;

public interface EmbeddedStorageBinaryTarget extends PersistenceTarget<Binary>
{
	@Override
	public void write(Binary data) throws PersistenceExceptionTransfer;



	public final class Implementation implements EmbeddedStorageBinaryTarget
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final StorageRequestAcceptor requestAcceptor;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Implementation(final StorageRequestAcceptor requestAcceptor)
		{
			super();
			this.requestAcceptor = requestAcceptor;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final void write(final Binary data) throws PersistenceExceptionTransfer
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
