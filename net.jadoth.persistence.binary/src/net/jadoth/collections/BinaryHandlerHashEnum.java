package net.jadoth.collections;

import java.util.Iterator;

import net.jadoth.functional._longProcedure;
import net.jadoth.memory.Memory;
import net.jadoth.memory.objectstate.ObjectState;
import net.jadoth.memory.objectstate.ObjectStateHandlerLookup;
import net.jadoth.persistence.binary.internal.AbstractBinaryHandlerNativeCustom;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryCollectionHandling;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.swizzling.types.Swizzle;
import net.jadoth.swizzling.types.SwizzleBuildLinker;
import net.jadoth.swizzling.types.SwizzleFunction;
import net.jadoth.swizzling.types.SwizzleStoreLinker;


/**
 *
 * @author Thomas Muenz
 */
public final class BinaryHandlerHashEnum extends AbstractBinaryHandlerNativeCustom<HashEnum<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	static final long BINARY_OFFSET_HASH_DENSITY =                       0;
	static final long BINARY_OFFSET_ELEMENTS     = Memory.byteSize_float(); // one float offset to sized array



	///////////////////////////////////////////////////////////////////////////
	// static methods    //
	/////////////////////

	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<HashEnum<?>> typeWorkaround()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)HashEnum.class;
	}

	private static long getBuildItemElementCount(final Binary bytes)
	{
		return BinaryPersistence.getListElementCount(bytes, BINARY_OFFSET_ELEMENTS);
	}

	private static float getBuildItemHashDensity(final Binary bytes)
	{
		return BinaryPersistence.get_float(bytes, BINARY_OFFSET_HASH_DENSITY);
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerHashEnum(final long typeId)
	{
		// binary layout definition
		super(
			typeId,
			typeWorkaround(),
			BinaryCollectionHandling.elementsPseudoFields(
				pseudoField(float.class, "hashDensity")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void store(
		final Binary             bytes    ,
		final HashEnum<?>        instance ,
		final long               oid      ,
		final SwizzleStoreLinker linker
	)
	{
		// store elements simply as array binary form
		final long contentAddress = BinaryCollectionHandling.storeSizedIterableAsList(
			bytes                 ,
			this.typeId()         ,
			oid                   ,
			BINARY_OFFSET_ELEMENTS,
			instance              ,
			instance.size()       ,
			linker
		);

		// store hash density as (sole) header value
		Memory.set_float(contentAddress, instance.hashDensity);
	}

	@Override
	public final HashEnum<?> create(final Binary bytes)
	{
		return HashEnum.NewCustom(
			getBuildItemElementCount(bytes),
			getBuildItemHashDensity(bytes)
		);
	}

	@Override
	public final void update(final Binary bytes, final HashEnum<?> instance, final SwizzleBuildLinker builder)
	{
		@SuppressWarnings("unchecked") // necessary because this handler operates on a generic technical level
		final HashEnum<Object> collectingInstance = (HashEnum<Object>)instance;

		// length must be checked for consistency reasons
		instance.ensureCapacity(getBuildItemElementCount(bytes));

		instance.size = BinaryPersistence.collectListObjectReferences(
			bytes                 ,
			BINARY_OFFSET_ELEMENTS,
			builder               ,
			collectingInstance::add
		);
		// note: hashDensity has already been set at creation time (shallow primitive value)
	}

	@Override
	public final void iterateInstanceReferences(final HashEnum<?> instance, final SwizzleFunction iterator)
	{
		Swizzle.iterateReferences(iterator, instance);
	}

	@Override
	public final void iteratePersistedReferences(final Binary bytes, final _longProcedure iterator)
	{
		BinaryPersistence.iterateListElementReferences(bytes, BINARY_OFFSET_ELEMENTS, iterator);
	}

	@Override
	public final boolean isEqual(
		final HashEnum<?>              source            ,
		final HashEnum<?>              target            ,
		final ObjectStateHandlerLookup stateHandlerLookup
	)
	{
		// one enum must be iterated with a stateful iterator while the other one is iterated directly
		final Iterator<?> srcIterator = source.iterator();
		return source.size == target.size && target.applies(
			e -> srcIterator.hasNext() && ObjectState.isEqual(e, srcIterator.next(), stateHandlerLookup)
		);
	}

	@Override
	public final boolean hasInstanceReferences()
	{
		return true;
	}

	@Override
	public final boolean isVariableBinaryLengthType()
	{
		return true;
	}

	@Override
	public final boolean hasVariableBinaryLengthInstances()
	{
		return true;
	}

}
