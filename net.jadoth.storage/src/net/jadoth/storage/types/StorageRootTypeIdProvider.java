package net.jadoth.storage.types;


public interface StorageRootTypeIdProvider
{
	public long provideRootTypeId();



	public final class Implementation implements StorageRootTypeIdProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final long rootTypeId;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		public Implementation(final long rootTypeId)
		{
			super();
			this.rootTypeId = rootTypeId;
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public final long provideRootTypeId()
		{
			return this.rootTypeId;
		}

	}

}
