package one.microstream.java.util;

import java.util.Properties;

import one.microstream.X;
import one.microstream.chars.XChars;
import one.microstream.collections.old.JavaUtilMapEntrySetFlattener;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryCollectionHandling;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerProperties extends AbstractBinaryHandlerCustomCollection<Properties>
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	static final long BINARY_OFFSET_DEFAULTS =                      0;
	static final long BINARY_OFFSET_ELEMENTS = Binary.oidByteLength();

	

	///////////////////////////////////////////////////////////////////////////
	// static methods    //
	/////////////////////

	private static Class<Properties> typeWorkaround()
	{
		return Properties.class; // no idea how to get ".class" to work otherwise
	}

	static final int getElementCount(final Binary bytes)
	{
		return X.checkArrayRange(bytes.getListElementCountReferences(BINARY_OFFSET_ELEMENTS));
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerProperties()
	{
		super(
			typeWorkaround(),
			BinaryCollectionHandling.simpleArrayPseudoFields(
				pseudoField(Properties.class, "defaults")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                  bytes   ,
		final Properties              instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		// (19.03.2019 TM)FIXME: MS-76: overhaul all java.util.Map-like handlers to use proper structure like in BinaryHandlerHashTable.
		
		final long contentAddress = bytes.storeSizedIterableAsList(
			this.typeId()         ,
			objectId              ,
			BINARY_OFFSET_ELEMENTS,
			() ->
				JavaUtilMapEntrySetFlattener.New(instance),
			instance.size() * 2   ,
			handler
		);

		bytes.store_long(
			contentAddress + BINARY_OFFSET_DEFAULTS,
			handler.apply(XMemory.accessDefaults(instance))
		);
	}
	
	@Override
	public final Properties create(final Binary bytes)
	{
		return new Properties();
	}

	@Override
	public final void update(final Binary bytes, final Properties instance, final PersistenceLoadHandler handler)
	{
		// the cast is important to ensure the type validity of the resolved defaults instance.
		XMemory.setDefaults(
			instance,
			(Properties)handler.lookupObject(bytes.get_long(BINARY_OFFSET_DEFAULTS))
		);
		
		final int      elementCount   = getElementCount(bytes);
		final Object[] elementsHelper = new Object[elementCount];
		
		bytes.collectElementsIntoArray(BINARY_OFFSET_ELEMENTS, handler, elementsHelper);
		bytes.registerHelper(instance, elementsHelper);
	}

	@Override
	public void complete(final Binary bytes, final Properties instance, final PersistenceLoadHandler loadHandler)
	{
		final Object helper = bytes.getHelper(instance);
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
		for(int i = 0; i < elementsHelper.length; i += 2)
		{
			if(instance.putIfAbsent(elementsHelper[i], elementsHelper[i + 1]) != null)
			{
				// (22.04.2016 TM)EXCP: proper exception
				throw new RuntimeException(
					"Element hashing inconsistency in " + XChars.systemString(instance)
				);
			}
		}
	}

	@Override
	public final void iterateInstanceReferences(final Properties instance, final PersistenceFunction iterator)
	{
		iterator.apply(XMemory.accessDefaults(instance));
		Persistence.iterateReferencesMap(iterator, instance);
	}

	@Override
	public final void iteratePersistedReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
	{
		iterator.acceptObjectId(bytes.get_long(BINARY_OFFSET_DEFAULTS));
		bytes.iterateListElementReferences(BINARY_OFFSET_ELEMENTS, iterator);
	}
	
}
