package one.microstream.util;

import one.microstream.collections.BinaryHandlerEqHashEnum;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustom;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
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
	// methods //
	////////////
	
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

	@Override
	public final void store(
		final Binary                  data    ,
		final Substituter.Default<?>  instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		synchronized(instance)
		{
			BinaryHandlerEqHashEnum.staticStore(data, instance.elements, this.typeId(), objectId, handler);
		}
	}

	@Override
	public final Substituter.Default<?> create(
		final Binary                 data   ,
		final PersistenceLoadHandler handler
	)
	{
		// hashEqualator gets set in update
		return new Substituter.Default<>(BinaryHandlerEqHashEnum.staticCreate(data));
	}

	@Override
	public final void updateState(
		final Binary                 data    ,
		final Substituter.Default<?> instance,
		final PersistenceLoadHandler handler
	)
	{
		synchronized(instance)
		{
			BinaryHandlerEqHashEnum.staticUpdate(data, instance.elements, handler);
		}
	}

	@Override
	public void complete(
		final Binary                 data    ,
		final Substituter.Default<?> instance,
		final PersistenceLoadHandler handler
	)
	{
		synchronized(instance)
		{
			BinaryHandlerEqHashEnum.staticComplete(data, instance.elements);
		}
	}

	@Override
	public final void iterateLoadableReferences(
		final Binary                     data    ,
		final PersistenceReferenceLoader iterator
	)
	{
		BinaryHandlerEqHashEnum.staticIteratePersistedReferences(data, iterator);
	}

}
