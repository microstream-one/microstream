package one.microstream.storage.types;


/**
 * The representation of a data file inside a transactions file, accumulated from multiple transactions entries.
 *
 * @author Thomas Muenz
 */
public interface StorageTransactionEntry
{
	public long fileNumber();

	public long length();

	public boolean isDeleted();
	
	public default boolean isEmpty()
	{
		return this.length() == 0L;
	}



	public final class Default implements StorageTransactionEntry
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

		public Default(final long fileNumber, final long length)
		{
			super();
			this.fileNumber = fileNumber;
			this.length     = length    ;
		}



		////////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

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
