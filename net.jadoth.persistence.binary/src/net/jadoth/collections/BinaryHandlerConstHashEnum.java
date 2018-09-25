package net.jadoth.collections;

import java.util.function.Consumer;

import net.jadoth.X;
import net.jadoth.functional._longProcedure;
import net.jadoth.low.XVM;
import net.jadoth.persistence.binary.internal.AbstractBinaryHandlerNativeCustomCollection;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryCollectionHandling;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.swizzling.types.Swizzle;
import net.jadoth.swizzling.types.SwizzleBuildLinker;
import net.jadoth.swizzling.types.SwizzleFunction;
import net.jadoth.swizzling.types.SwizzleHandler;


/**
 *
 * @author Thomas Muenz
 */
public final class BinaryHandlerConstHashEnum
extends AbstractBinaryHandlerNativeCustomCollection<ConstHashEnum<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	static final long BINARY_OFFSET_HASH_DENSITY =                       0;
	static final long BINARY_OFFSET_ELEMENTS     = XVM.byteSize_float(); // one float offset to sized array



	///////////////////////////////////////////////////////////////////////////
	// static methods    //
	/////////////////////

	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<ConstHashEnum<?>> typeWorkaround()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)ConstHashEnum.class;
	}

	private static int getBuildItemElementCount(final Binary bytes)
	{
		return X.checkArrayRange(BinaryPersistence.getListElementCount(bytes, BINARY_OFFSET_ELEMENTS));
	}

	private static float getBuildItemHashDensity(final Binary bytes)
	{
		return BinaryPersistence.get_float(bytes, BINARY_OFFSET_HASH_DENSITY);
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerConstHashEnum()
	{
		// binary layout definition
		super(
			typeWorkaround(),
			BinaryCollectionHandling.elementsPseudoFields(pseudoField(float.class, "hashDensity"))
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary           bytes   ,
		final ConstHashEnum<?> instance,
		final long             oid     ,
		final SwizzleHandler   handler
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
			handler
		);

		// store hash density as (sole) header value
		XVM.set_float(contentAddress, instance.hashDensity);
	}

	@Override
	public final ConstHashEnum<?> create(final Binary bytes)
	{
		return ConstHashEnum.NewCustom(
			getBuildItemElementCount(bytes),
			getBuildItemHashDensity(bytes)
		);
	}

	@Override
	public final void update(final Binary bytes, final ConstHashEnum<?> instance, final SwizzleBuildLinker builder)
	{
		@SuppressWarnings("unchecked") // necessary because this handler operates on a generic technical level
		final ConstHashEnum<Object> collectingInstance = (ConstHashEnum<Object>)instance;

		// validate to the best of possibilities
		if(instance.size != 0)
		{
			throw new IllegalStateException(); // (26.10.2013)EXCP: proper exception
		}

		instance.size = BinaryPersistence.collectListObjectReferences(
			bytes                 ,
			BINARY_OFFSET_ELEMENTS,
			builder               ,
			new Consumer<Object>()
			{
				@Override
				public void accept(final Object e)
				{
					collectingInstance.internalAdd(e);
				}
			}
		);
		// note: hashDensity has already been set at creation time (shallow primitive value)
	}

	@Override
	public final void iterateInstanceReferences(final ConstHashEnum<?> instance, final SwizzleFunction iterator)
	{
		Swizzle.iterateReferences(iterator, instance);
	}

	@Override
	public final void iteratePersistedReferences(final Binary bytes, final _longProcedure iterator)
	{
		BinaryPersistence.iterateListElementReferences(bytes, BINARY_OFFSET_ELEMENTS, iterator);
	}

}
