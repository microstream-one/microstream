package one.microstream.util;

import one.microstream.collections.BinaryHandlerEqHashEnum;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustom;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceObjectIdResolver;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceStoreHandler;


/**
 *
 * @author Thomas Muenz
 */
public final class BinaryHandlerSubstituterDefault
extends AbstractBinaryHandlerCustom<Substituter.Default<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<Substituter.Default<?>> handledType()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)Substituter.Default.class;
	}
	
	public static BinaryHandlerSubstituterDefault New()
	{
		return new BinaryHandlerSubstituterDefault();
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerSubstituterDefault()
	{
		// binary layout definition
		super(
			handledType(),
			BinaryHandlerEqHashEnum.Fields()
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void store(
		final Binary                  bytes   ,
		final Substituter.Default<?>  instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		synchronized(instance)
		{
			BinaryHandlerEqHashEnum.staticStore(bytes, instance.elements, this.typeId(), objectId, handler);
		}
	}

	@Override
	public final Substituter.Default<?> create(
		final Binary                      bytes     ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		// hashEqualator gets set in update
		return new Substituter.Default<>(BinaryHandlerEqHashEnum.staticCreate(bytes));
	}

	@Override
	public final void update(
		final Binary                      bytes     ,
		final Substituter.Default<?>      instance  ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		synchronized(instance)
		{
			BinaryHandlerEqHashEnum.staticUpdate(bytes, instance.elements, idResolver);
		}
	}

	@Override
	public void complete(
		final Binary                      medium    ,
		final Substituter.Default<?>      instance  ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		synchronized(instance)
		{
			BinaryHandlerEqHashEnum.staticComplete(medium, instance.elements);
		}
	}

	@Override
	public final void iterateLoadableReferences(
		final Binary                     bytes   ,
		final PersistenceReferenceLoader iterator
	)
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

}
