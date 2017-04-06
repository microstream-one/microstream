package net.jadoth.util;

import net.jadoth.collections.BinaryHandlerEqHashEnum;
import net.jadoth.functional._longProcedure;
import net.jadoth.memory.objectstate.ObjectStateHandlerLookup;
import net.jadoth.persistence.binary.internal.AbstractBinaryHandlerNativeCustom;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.swizzling.types.SwizzleBuildLinker;
import net.jadoth.swizzling.types.SwizzleStoreLinker;


/**
 *
 * @author Thomas Muenz
 */
public final class BinaryHandlerSubstituterImplementation
extends AbstractBinaryHandlerNativeCustom<Substituter.Implementation<?>>
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

	public BinaryHandlerSubstituterImplementation(final long typeId)
	{
		// binary layout definition
		super(
			typeId,
			typeWorkaround(),
			BinaryHandlerEqHashEnum.pseudoFields()
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void store(
		final Binary                        bytes    ,
		final Substituter.Implementation<?> instance ,
		final long                          oid      ,
		final SwizzleStoreLinker               linker
	)
	{
		synchronized(instance)
		{
			BinaryHandlerEqHashEnum.staticStore(bytes, instance.elements, this.typeId(), oid, linker);
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
		final SwizzleBuildLinker            builder
	)
	{
		synchronized(instance)
		{
			BinaryHandlerEqHashEnum.staticUpdate(bytes, instance.elements, builder);
		}
	}

	@Override
	public void complete(
		final Binary                        medium  ,
		final Substituter.Implementation<?> instance,
		final SwizzleBuildLinker            builder
	)
	{
		synchronized(instance)
		{
			BinaryHandlerEqHashEnum.staticComplete(medium, instance.elements);
		}
	}

	@Override
	public final void iteratePersistedReferences(final Binary bytes, final _longProcedure iterator)
	{
		BinaryHandlerEqHashEnum.staticIteratePersistedReferences(bytes, iterator);
	}

	@Override
	public final boolean isEqual(
		final Substituter.Implementation<?> source            ,
		final Substituter.Implementation<?> target            ,
		final ObjectStateHandlerLookup      stateHandlerLookup
	)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME BinaryHandlerSubstituterImplementation#isEqual()
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
