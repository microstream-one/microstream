package net.jadoth.persistence.binary.internal;


import net.jadoth.X;
import net.jadoth.collections.Constant;
import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.persistence.types.PersistenceTypeDefinitionMemberPrimitiveDefinition;
import net.jadoth.swizzling.types.SwizzleBuildLinker;
import net.jadoth.swizzling.types.SwizzleHandler;


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
	public void store(final Binary bytes, final T instance, final long oid, final SwizzleHandler handler)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public T create(final Binary bytes)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void update(final Binary bytes, final T instance, final SwizzleBuildLinker builder)
	{
		throw new UnsupportedOperationException();
	}


}
