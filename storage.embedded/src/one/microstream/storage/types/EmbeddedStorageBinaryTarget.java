package one.microstream.storage.types;

import static one.microstream.X.notNull;

import one.microstream.afs.WriteController;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.exceptions.PersistenceExceptionTransfer;
import one.microstream.persistence.types.PersistenceTarget;

public interface EmbeddedStorageBinaryTarget extends PersistenceTarget<Binary>
{
	@Override
	public void write(Binary data) throws PersistenceExceptionTransfer;


	
	public static EmbeddedStorageBinaryTarget New(
		final StorageRequestAcceptor requestAcceptor,
		final WriteController        writeController
	)
	{
		return new EmbeddedStorageBinaryTarget.Default(
			notNull(requestAcceptor),
			notNull(writeController)
		);
	}

	public final class Default implements EmbeddedStorageBinaryTarget
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final StorageRequestAcceptor requestAcceptor;
		private final WriteController        writeController;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final StorageRequestAcceptor requestAcceptor,
			final WriteController        writeController
		)
		{
			super();
			this.requestAcceptor = requestAcceptor;
			this.writeController = writeController;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final void write(final Binary data) throws PersistenceExceptionTransfer
		{
			try
			{
				this.writeController.validateIsWritable();
				this.requestAcceptor.storeData(data);
			}
			catch(final Exception e)
			{
				throw new PersistenceExceptionTransfer(e);
			}
		}
		
		@Override
		public final void validateIsWritable()
		{
			this.writeController.validateIsWritable();
		}
		
		@Override
		public final boolean isWritable()
		{
			return this.writeController.isWritable();
		}

	}

}
