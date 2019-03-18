package one.microstream.java.util;

import java.util.HashSet;

import one.microstream.X;
import one.microstream.chars.XChars;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryCollectionHandling;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerHashSet extends AbstractBinaryHandlerCustomCollection<HashSet<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	static final long BINARY_OFFSET_LOAD_FACTOR =           0; // 1 float at offset 0
	static final long BINARY_OFFSET_ELEMENTS    = Float.BYTES; // sized array at offset 0 + float size



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
		return bytes.get_float(BINARY_OFFSET_LOAD_FACTOR);
	}

	static final int getElementCount(final Binary bytes)
	{
		return X.checkArrayRange(bytes.getListElementCountReferences(BINARY_OFFSET_ELEMENTS));
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
	public final void store(final Binary bytes, final HashSet<?> instance, final long oid, final PersistenceStoreHandler handler)
	{
		// store elements simply as array binary form
		final long contentAddress = bytes.storeSizedIterableAsList(
			this.typeId()         ,
			oid                   ,
			BINARY_OFFSET_ELEMENTS,
			instance              ,
			instance.size()       ,
			handler
		);

		// store load factor as (sole) header value
		bytes.store_float(contentAddress, XMemory.accessLoadFactor(instance));
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
	public final void update(final Binary rawData, final HashSet<?> instance, final PersistenceLoadHandler builder)
	{
		final int      elementCount   = getElementCount(rawData);
		final Object[] elementsHelper = new Object[elementCount];
		
		rawData.collectElementsIntoArray(BINARY_OFFSET_ELEMENTS, builder, elementsHelper);
	
		rawData.registerHelper(instance, elementsHelper);
	}

	@Override
	public void complete(final Binary rawData, final HashSet<?> instance, final PersistenceLoadHandler loadHandler)
	{
		final Object helper = rawData.getHelper(instance);
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
				throw new RuntimeException(
					"Element hashing inconsistency in " + XChars.systemString(castedInstance)
				);
			}
		}
		
		rawData.registerHelper(instance, null); // might help Garbage Collector
	}

	@Override
	public final void iterateInstanceReferences(final HashSet<?> instance, final PersistenceFunction iterator)
	{
		for(final Object e : instance)
		{
			iterator.apply(e);
		}
	}

	@Override
	public final void iteratePersistedReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
	{
		bytes.iterateListElementReferences(BINARY_OFFSET_ELEMENTS, iterator);
	}

}
