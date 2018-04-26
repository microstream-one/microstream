package net.jadoth.persistence.binary.internal;


import net.jadoth.collections.Constant;
import net.jadoth.collections.X;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.memory.objectstate.ObjectStateHandlerLookup;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMember;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMemberPrimitiveDefinition;
import net.jadoth.swizzling.types.SwizzleBuildLinker;
import net.jadoth.swizzling.types.PersistenceStoreFunction;


public final class BinaryHandlerPrimitive<T> extends AbstractBinaryHandlerTrivial<T>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final Constant<PersistenceTypeDescriptionMemberPrimitiveDefinition> member;
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerPrimitive(final Class<T> type, final long typeId)
	{
		super(type, typeId);

		final long primitiveBinaryLength = BinaryPersistence.resolvePrimitiveFieldBinaryLength(type);

		this.member = X.Constant(
			new PersistenceTypeDescriptionMemberPrimitiveDefinition.Implementation(
				type,
				primitiveBinaryLength,
				primitiveBinaryLength
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public void store(final Binary bytes, final T instance, final long oid, final PersistenceStoreFunction linker)
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

	@Override
	public boolean isEqual(final T source, final T target, final ObjectStateHandlerLookup instanceStateHandlerLookup)
	{
		return source == target;
	}

	@Override
	public final XGettingSequence<? extends PersistenceTypeDescriptionMember> members()
	{
		return this.member;
	}
	
	@Override
	public final boolean isPrimitiveType()
	{
		return true;
	}


}
