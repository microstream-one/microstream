package one.microstream.persistence.types;

import static one.microstream.X.notNull;

import one.microstream.collections.Set_long;
import one.microstream.functional._longPredicate;

public interface ObjectIdSelection extends _longPredicate
{
	@Override
	public boolean test(long objectId);



	public static ObjectIdSelection New(final Set_long objectIds)
	{
		return new ObjectIdSelection.Default(
			notNull(objectIds)
		);
	}

	public final class Default implements ObjectIdSelection
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final Set_long objectIds;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final Set_long objectIds)
		{
			super();
			this.objectIds = objectIds;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final boolean test(final long objectId)
		{
			return this.objectIds.contains(objectId);
		}

	}

}
