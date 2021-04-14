package one.microstream.storage.types;


public interface StorageRootTypeIdProvider
{
	public long provideRootTypeId();



	public final class Default implements StorageRootTypeIdProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final long rootTypeId;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Default(final long rootTypeId)
		{
			super();
			this.rootTypeId = rootTypeId;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final long provideRootTypeId()
		{
			return this.rootTypeId;
		}

	}

}
