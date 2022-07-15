package one.microstream.persistence.binary.internal;

/*-
 * #%L
 * microstream-persistence-binary
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

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
	
	protected static long getNameObjectId(final Binary data)
	{
		return data.read_long(BINARY_OFFSET_NAME);
	}
	
	protected static int getOrdinalValue(final Binary data)
	{
		return data.read_int(BINARY_OFFSET_ORDINAL);
	}
	
	@Override
	protected String getName(final Binary data, final PersistenceLoadHandler handler)
	{
		return (String)handler.lookupObject(getNameObjectId(data));
	}
	
	@Override
	protected int getOrdinal(final Binary data)
	{
		return getOrdinalValue(data);
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
		final Binary                          data    ,
		final T                               instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.storeEntityHeader(BINARY_LENGTH, this.typeId(), objectId);
		
		data.store_long(BINARY_OFFSET_NAME   , handler.apply(instance.name()));
		data.store_long(BINARY_OFFSET_ORDINAL, instance.ordinal()            );
	}

	@Override
	public void iterateLoadableReferences(
		final Binary                     data    ,
		final PersistenceReferenceLoader iterator
	)
	{
		iterator.acceptObjectId(getNameObjectId(data));
	}
	
}
