package one.microstream.util;

import one.microstream.collections.BinaryHandlerEqHashEnum;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustom;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceStoreHandler;
import one.microstream.util.Substituter;


/**
 *
 * @author Thomas Muenz
 */
public final class BinaryHandlerSubstituterImplementation
extends AbstractBinaryHandlerCustom<Substituter.Implementation<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods    //
	/////////////////////

	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<Substituter.Implementation<?>> typeWorkaround()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)Substituter.Implementation.class;
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerSubstituterImplementation()
	{
		// binary layout definition
		super(
			typeWorkaround(),
			BinaryHandlerEqHashEnum.pseudoFields()
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void store(
		final Binary                        bytes   ,
		final Substituter.Implementation<?> instance,
		final long                          oid     ,
		final PersistenceStoreHandler                handler
	)
	{
		synchronized(instance)
		{
			BinaryHandlerEqHashEnum.staticStore(bytes, instance.elements, this.typeId(), oid, handler);
		}
	}

	@Override
	public final Substituter.Implementation<?> create(final Binary bytes)
	{
		// hashEqualator gets set in update
		return new Substituter.Implementation<>(BinaryHandlerEqHashEnum.staticCreate(bytes));
	}

	@Override
	public final void update(
		final Binary                        bytes   ,
		final Substituter.Implementation<?> instance,
		final PersistenceLoadHandler        handler
	)
	{
		synchronized(instance)
		{
			BinaryHandlerEqHashEnum.staticUpdate(bytes, instance.elements, handler);
		}
	}

	@Override
	public void complete(
		final Binary                        medium  ,
		final Substituter.Implementation<?> instance,
		final PersistenceLoadHandler        handler
	)
	{
		synchronized(instance)
		{
			BinaryHandlerEqHashEnum.staticComplete(medium, instance.elements);
		}
	}

	@Override
	public final void iteratePersistedReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
	{
		BinaryHandlerEqHashEnum.staticIteratePersistedReferences(bytes, iterator);
	}

	@Override
	public final boolean hasInstanceReferences()
	{
		return true;
	}
	
	@Override
	public final boolean hasPersistedReferences()
	{
		return true;
	}
	
	@Override
	public final boolean hasPersistedVariableLength()
	{
		return true;
	}

	@Override
	public final boolean hasVaryingPersistedLengthInstances()
	{
		return true;
	}

//	@Override
//	public final void copy(final Substituter.Implementation<?> source, final Substituter.Implementation<?> target)
//	{
//		// due to type erasure, there is no way to determine if target is valid.
//		// this also proces that such a totaly generic copy functionality is not viable here
//		throw new UnsupportedOperationException();
//	}

}
