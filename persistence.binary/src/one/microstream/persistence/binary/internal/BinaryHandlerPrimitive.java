package one.microstream.persistence.binary.internal;


import one.microstream.X;
import one.microstream.collections.Constant;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryPersistence;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberPrimitiveDefinition;


public final class BinaryHandlerPrimitive<T> extends AbstractBinaryHandlerTrivial<T>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final Constant<PersistenceTypeDefinitionMemberPrimitiveDefinition> member;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerPrimitive(final Class<T> type)
	{
		super(type);

		final long primitiveBinaryLength = BinaryPersistence.resolvePrimitiveFieldBinaryLength(type);
		this.member = X.Constant(
			PersistenceTypeDefinitionMemberPrimitiveDefinition.New(
				type,
				primitiveBinaryLength,
				primitiveBinaryLength
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final XGettingEnum<? extends PersistenceTypeDefinitionMemberPrimitiveDefinition> members()
	{
		return this.member;
	}
	
	@Override
	public final long membersPersistedLengthMinimum()
	{
		return this.member.get().persistentMinimumLength();
	}
	
	@Override
	public final long membersPersistedLengthMaximum()
	{
		return this.member.get().persistentMaximumLength();
	}
	
	@Override
	public final boolean isPrimitiveType()
	{
		return true;
	}

	@Override
	public void store(final Binary bytes, final T instance, final long oid, final PersistenceStoreHandler handler)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public T create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void update(final Binary bytes, final T instance, final PersistenceLoadHandler handler)
	{
		throw new UnsupportedOperationException();
	}


}
