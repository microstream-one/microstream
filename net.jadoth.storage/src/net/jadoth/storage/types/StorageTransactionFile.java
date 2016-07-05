package net.jadoth.storage.types;


/**
 * The representation of a data file inside a transactions file, accumulated from multiple transactions entry.
 *
 * @author Paigan
 */
public interface StorageTransactionFile
{
	public long fileNumber();

	public long length();

	public boolean isDeleted();



	public final class Implementation implements StorageTransactionFile
	{
		////////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final long fileNumber;
		final long length    ;

		boolean isDeleted;



		////////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Implementation(final long fileNumber, final long length)
		{
			super();
			this.fileNumber = fileNumber;
			this.length     = length    ;
		}



		////////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public final long fileNumber()
		{
			return this.fileNumber;
		}

		@Override
		public final long length()
		{
			return this.length;
		}

		@Override
		public final boolean isDeleted()
		{
			return this.isDeleted;
		}

	}

}
