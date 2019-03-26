package one.microstream.storage.types;

public interface StorageInventoryEntity
{
	public long position();

	public long length();

	public long typeId();

	public long objectId();

	public StorageInventoryEntity next();



	public final class Implementation implements StorageInventoryEntity
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private final long position, length, typeId, objectId;
		private       StorageInventoryEntity.Implementation next;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Implementation(final long position, final long length, final long typeId, final long objectId)
		{
			super();
			this.position = position;
			this.length   = length  ;
			this.typeId   = typeId  ;
			this.objectId = objectId;
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		final void setNext(final StorageInventoryEntity.Implementation next)
		{
			this.next = next;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final long position()
		{
			return this.position;
		}

		@Override
		public final long length()
		{
			return this.length;
		}

		@Override
		public final long typeId()
		{
			return this.typeId;
		}

		@Override
		public final long objectId()
		{
			return this.objectId;
		}

		@Override
		public final StorageInventoryEntity.Implementation next()
		{
			return this.next;
		}

		@Override
		public final String toString()
		{
			return "Init Entity " + this.length + ", " + this.typeId + ", " + this.objectId + " @ " + this.position;
		}

	}

}
