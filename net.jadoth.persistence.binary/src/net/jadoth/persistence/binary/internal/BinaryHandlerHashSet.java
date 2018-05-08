package net.jadoth.persistence.binary.internal;

import java.util.HashSet;

import net.jadoth.X;
import net.jadoth.chars.XChars;
import net.jadoth.functional._longProcedure;
import net.jadoth.memory.Memory;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryCollectionHandling;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.swizzling.types.PersistenceStoreFunction;
import net.jadoth.swizzling.types.SwizzleBuildLinker;
import net.jadoth.swizzling.types.SwizzleFunction;


public final class BinaryHandlerHashSet extends AbstractBinaryHandlerNativeCustomCollection<HashSet<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	static final long BINARY_OFFSET_LOAD_FACTOR =                       0; // 1 float at offset 0
	static final long BINARY_OFFSET_ELEMENTS    = Memory.byteSize_float(); // sized array at offset 0 + float size



	///////////////////////////////////////////////////////////////////////////
	// static methods    //
	/////////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<HashSet<?>> typeWorkaround()
	{
		return (Class)HashSet.class; // no idea how to get ".class" to work otherwise
	}

	static final float getLoadFactor(final Binary bytes)
	{
		return BinaryPersistence.get_float(bytes, BINARY_OFFSET_LOAD_FACTOR);
	}

	static final int getElementCount(final Binary bytes)
	{
		return X.checkArrayRange(BinaryPersistence.getListElementCount(bytes, BINARY_OFFSET_ELEMENTS));
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerHashSet()
	{
		super(
			typeWorkaround(),
			BinaryCollectionHandling.elementsPseudoFields(
				pseudoField(float.class, "loadFactor")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary             bytes   ,
		final HashSet<?>         instance,
		final long               oid     ,
		final PersistenceStoreFunction linker
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

		// store load factor as (sole) header value
		Memory.set_float(contentAddress, Memory.accessLoadFactor(instance));
	}

	@Override
	public final HashSet<?> create(final Binary bytes)
	{
		return new HashSet<>(
			getElementCount(bytes),
			getLoadFactor(bytes)
		);
	}

	@Override
	public final void update(final Binary bytes, final HashSet<?> instance, final SwizzleBuildLinker builder)
	{
		final int elementCount = getElementCount(bytes);
		final Object[] elementsHelper = new Object[elementCount];
		
		BinaryPersistence.collectElementsIntoArray(bytes, BINARY_OFFSET_ELEMENTS, builder, elementsHelper);
	
		builder.registerHelper(instance, elementsHelper);
	}

	@Override
	public final void iterateInstanceReferences(final HashSet<?> instance, final SwizzleFunction iterator)
	{
		for(final Object e : instance)
		{
			iterator.apply(e);
		}
	}

	@Override
	public final void iteratePersistedReferences(final Binary bytes, final _longProcedure iterator)
	{
		BinaryPersistence.iterateListElementReferences(bytes, BINARY_OFFSET_ELEMENTS, iterator);
	}

	@Override
	public void complete(final Binary medium, final HashSet<?> instance, final SwizzleBuildLinker builder)
	{
		final Object helper = builder.getHelper(instance);
		
		if(helper == null)
		{
			// (22.04.2016 TM)EXCP: proper exception
			throw new RuntimeException(
				"Missing element collection helper instance for " + XChars.systemString(instance)
			);
		}
		
		if(!(helper instanceof Object[]))
		{
			// (22.04.2016 TM)EXCP: proper exception
			throw new RuntimeException(
				"Inconsistent element collection helper instance for " + XChars.systemString(instance)
			);
		}
		
		final Object[] elementsHelper = (Object[])helper;
		@SuppressWarnings("unchecked")
		final HashSet<Object> castedInstance = (HashSet<Object>)instance;
		
		for(final Object element : elementsHelper)
		{
			/* (22.04.2016 TM)NOTE: oh look, they added an add() logic complementary to put().
			 * I did that years ago as a noob.
			 * They even chose the proper reasonable term instead of the moronic "putIfAbsent"
			 * or some "putElementOnlyIfAbsentBecauseWeLikeMoronicNaming" terminology normally to be expected
			 * from the JDK.
			 * If they now also realize that their collection's hash-equality, immutability and most other concepts
			 * are deeply flawed, they might end up developing a proper collection framework. In 50 years or so.
			 */
			if(!castedInstance.add(element))
			{
				// (22.04.2016 TM)EXCP: proper exception
				throw new RuntimeException("Element hashing insistency in " + XChars.systemString(castedInstance));
			}
		}
	}

}
