package one.microstream.jdk8.java.util;

import java.util.Properties;

import one.microstream.X;
import one.microstream.collections.old.KeyValueFlatCollector;
import one.microstream.collections.old.OldCollections;
import one.microstream.memory.XMemoryJDK8;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceObjectIdResolver;
import one.microstream.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerProperties extends AbstractBinaryHandlerCustomCollection<Properties>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	// no load factor because the Properties class does not allow to specify one. It is always the Hashtable default.
	static final long BINARY_OFFSET_DEFAULTS =                                                    0;
	static final long BINARY_OFFSET_ELEMENTS = BINARY_OFFSET_DEFAULTS + Binary.objectIdByteLength();



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	private static Class<Properties> typeWorkaround()
	{
		return Properties.class;
	}

	static final int getElementCount(final Binary bytes)
	{
		return X.checkArrayRange(bytes.getListElementCountKeyValue(BINARY_OFFSET_ELEMENTS));
	}
	
	public static BinaryHandlerProperties New()
	{
		return new BinaryHandlerProperties();
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerProperties()
	{
		super(
			typeWorkaround(),
			keyValuesFields(
				CustomField(Properties.class, "defaults")
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
		// store elements simply as array binary form
		bytes.storeMapEntrySet(
			this.typeId()         ,
			objectId              ,
			BINARY_OFFSET_ELEMENTS,
			instance.entrySet()   ,
			handler
		);

		bytes.store_long(
			BINARY_OFFSET_DEFAULTS,
			handler.apply(XMemoryJDK8.accessDefaults(instance))
		);
	}
	

	@Override
	public final Properties create(final Binary bytes, final PersistenceObjectIdResolver idResolver)
	{
		return new Properties();
	}

	@Override
	public final void update(
		final Binary                      bytes     ,
		final Properties                  instance  ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		instance.clear();
		
		final Object defaults = idResolver.lookupObject(bytes.get_long(BINARY_OFFSET_DEFAULTS));
		
		// the cast is important to ensure the type validity of the resolved defaults instance.
		XMemoryJDK8.setDefaults(instance, (Properties)defaults);
		
		final int elementCount = getElementCount(bytes);
		final KeyValueFlatCollector<Object, Object> collector = KeyValueFlatCollector.New(elementCount);
		bytes.collectKeyValueReferences(BINARY_OFFSET_ELEMENTS, elementCount, idResolver, collector);
		bytes.registerHelper(instance, collector.yield());
	}

	@Override
	public void complete(final Binary bytes, final Properties instance, final PersistenceObjectIdResolver idResolver)
	{
		OldCollections.populateMapFromHelperArray(instance, bytes.getHelper(instance));
	}

	@Override
	public final void iterateInstanceReferences(final Properties instance, final PersistenceFunction iterator)
	{
		iterator.apply(XMemoryJDK8.accessDefaults(instance));
		Persistence.iterateReferencesMap(iterator, instance);
	}
	
	@Override
	public final void iterateLoadableReferences(final Binary bytes, final PersistenceObjectIdAcceptor iterator)
	{
		iterator.acceptObjectId(bytes.get_long(BINARY_OFFSET_DEFAULTS));
		bytes.iterateKeyValueEntriesReferences(BINARY_OFFSET_ELEMENTS, iterator);
	}
	
}
