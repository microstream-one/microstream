package one.microstream.persistence.binary.internal;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryPersistence;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceStoreHandler;

public class BinaryHandlerCustomEnumTrivial<T extends Enum<T>> extends AbstractBinaryHandlerCustomEnum<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////
	
	// should they change in the JDK, these will still work, no problem.
	private static final String
		JAVA_LANG_ENUM_FIELD_NAME_NAME    = "name"   ,
		JAVA_LANG_ENUM_FIELD_NAME_ORDINAL = "ordinal"
	;
	
	private static final long
		BINARY_OFFSET_NAME    = 0                                                                               ,
		BINARY_OFFSET_ORDINAL = BINARY_OFFSET_NAME    + BinaryPersistence.resolveFieldBinaryLength(String.class),
		BINARY_LENGTH         = BINARY_OFFSET_ORDINAL + BinaryPersistence.resolveFieldBinaryLength(int.class)
	;
	
		
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	protected BinaryHandlerCustomEnumTrivial(final Class<T> type)
	{
		this(type, deriveTypeName(type));
	}
	
	protected BinaryHandlerCustomEnumTrivial(
		final Class<T> type    ,
		final String   typeName
	)
	{
		super(type, typeName, BinaryHandlerGenericEnum.deriveEnumConstantMembers(type),
			CustomFields(
				CustomField(String.class, JAVA_LANG_ENUM_FIELD_NAME_NAME),
				CustomField(int.class, JAVA_LANG_ENUM_FIELD_NAME_ORDINAL)
			)
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	protected static long getNameObjectId(final Binary bytes)
	{
		return bytes.read_long(BINARY_OFFSET_NAME);
	}
	
	protected static int getOrdinalValue(final Binary bytes)
	{
		return bytes.read_int(BINARY_OFFSET_ORDINAL);
	}
	
	@Override
	protected String getName(final Binary bytes, final PersistenceLoadHandler handler)
	{
		return (String)handler.lookupObject(getNameObjectId(bytes));
	}
	
	@Override
	protected int getOrdinal(final Binary bytes)
	{
		return getOrdinalValue(bytes);
	}

	@Override
	public boolean hasInstanceReferences()
	{
		return true;
	}

	@Override
	public void iterateLoadableReferences(
		final Binary                     bytes   ,
		final PersistenceReferenceLoader iterator
	)
	{
		iterator.acceptObjectId(getNameObjectId(bytes));
	}

	@Override
	public boolean hasPersistedReferences()
	{
		return true;
	}

	@Override
	public boolean hasVaryingPersistedLengthInstances()
	{
		return false;
	}

	@Override
	public void store(
		final Binary                  bytes   ,
		final T                       instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		bytes.storeEntityHeader(BINARY_LENGTH, this.typeId(), objectId);
		
		bytes.store_long(BINARY_OFFSET_NAME   , handler.apply(instance.name()));
		bytes.store_long(BINARY_OFFSET_ORDINAL, instance.ordinal()            );
	}
	
}
