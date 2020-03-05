package one.microstream.persistence.types;

import one.microstream.persistence.binary.types.Binary;

public interface BinaryRootReferenceProvider<R extends PersistenceRootReference>
extends PersistenceRootReferenceProvider<Binary>
{
	public static BinaryRootReferenceProvider<PersistenceRootReference.Default> New()
	{
		return new BinaryRootReferenceProvider.Default(
			new PersistenceRootReference.Default(null)
		);
	}
	
	public final class Default implements BinaryRootReferenceProvider<PersistenceRootReference.Default>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final PersistenceRootReference.Default rootReference;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final PersistenceRootReference.Default rootReference)
		{
			super();
			this.rootReference = rootReference;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public PersistenceRootReference provideRootReference()
		{
			return this.rootReference;
		}
		
		@Override
		public PersistenceTypeHandler<Binary, ? extends PersistenceRootReference> provideTypeHandler(
			final PersistenceObjectRegistry globalRegistry
		)
		{
			return new BinaryHandlerRootReferenceDefault(this.rootReference, globalRegistry);
		}
		
	}
	
}
