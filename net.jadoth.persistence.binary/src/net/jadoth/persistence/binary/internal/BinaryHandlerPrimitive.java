package net.jadoth.persistence.binary.internal;


import net.jadoth.collections.X;
import net.jadoth.memory.objectstate.ObjectStateHandlerLookup;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.persistence.types.PersistenceTypeDescription;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMemberPrimitiveDefinition;
import net.jadoth.swizzling.types.SwizzleBuildLinker;
import net.jadoth.swizzling.types.SwizzleStoreLinker;


public final class BinaryHandlerPrimitive<T> extends AbstractBinaryHandlerTrivial<T>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private final PersistenceTypeDescription<T> typeDefinition;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerPrimitive(final Class<T> type, final long typeId)
	{
		super(type, typeId);

		final long primitiveBinaryLength = BinaryPersistence.resolvePrimitiveFieldBinaryLength(type);

		this.typeDefinition = PersistenceTypeDescription.New(
			typeId,
			type.getName(),
			type,
			X.Constant(
				new PersistenceTypeDescriptionMemberPrimitiveDefinition.Implementation(
					type,
					primitiveBinaryLength,
					primitiveBinaryLength
				)
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public void store(final Binary bytes, final T instance, final long oid, final SwizzleStoreLinker linker)
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
	public final boolean hasInstanceReferences()
	{
		return false;
	}

	@Override
	public PersistenceTypeDescription<T> typeDescription()
	{
		return this.typeDefinition;
	}

}
