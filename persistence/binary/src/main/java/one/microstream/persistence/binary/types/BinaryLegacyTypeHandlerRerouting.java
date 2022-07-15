package one.microstream.persistence.binary.types;

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

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import java.nio.ByteBuffer;

import one.microstream.collections.types.XGettingEnum;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.internal.AbstractBinaryLegacyTypeHandlerTranslating;
import one.microstream.persistence.types.PersistenceLegacyTypeHandlingListener;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeHandler;
import one.microstream.typing.KeyValue;

public final class BinaryLegacyTypeHandlerRerouting<T>
extends AbstractBinaryLegacyTypeHandlerTranslating<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static <T> BinaryLegacyTypeHandlerRerouting<T> New(
		final PersistenceTypeDefinition                       typeDefinition              ,
		final PersistenceTypeHandler<Binary, T>               typeHandler                 ,
		final XGettingEnum<KeyValue<Long, BinaryValueSetter>> translatorsWithTargetOffsets,
		final PersistenceLegacyTypeHandlingListener<Binary>   listener                    ,
		final boolean                                         switchByteOrder
	)
	{
		return new BinaryLegacyTypeHandlerRerouting<>(
			notNull(typeDefinition)                      ,
			notNull(typeHandler)                         ,
			toTranslators(translatorsWithTargetOffsets)  ,
			toTargetOffsets(translatorsWithTargetOffsets),
			mayNull(listener)                            ,
			switchByteOrder
		);
	}


	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final BinaryReferenceTraverser[] newBinaryLayoutReferenceTraversers;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryLegacyTypeHandlerRerouting(
		final PersistenceTypeDefinition                     typeDefinition  ,
		final PersistenceTypeHandler<Binary, T>             typeHandler     ,
		final BinaryValueSetter[]                           valueTranslators,
		final long[]                                        targetOffsets   ,
		final PersistenceLegacyTypeHandlingListener<Binary> listener        ,
		final boolean                                       switchByteOrder
	)
	{
		super(typeDefinition, typeHandler, valueTranslators, targetOffsets, listener, switchByteOrder);

		/* (01.01.2020 TM)NOTE: Bugfix:
		 * Moved from AbstractBinaryLegacyTypeHandlerTranslating here as this is only correct for ~Rerouting
		 * but incorrect for ~Reflective LegacyHandler
		 */
		// (12.11.2019 TM)NOTE: must be derived from the NEW type definition since #create relayouts the load data.
		this.newBinaryLayoutReferenceTraversers = deriveReferenceTraversers(typeHandler, switchByteOrder);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void iterateLoadableReferences(final Binary rawData, final PersistenceReferenceLoader iterator)
	{
		rawData.iterateReferences(this.newBinaryLayoutReferenceTraversers, iterator);
	}

	@Override
	protected T internalCreate(final Binary rawData, final PersistenceLoadHandler handler)
	{
		final long entityContentLength = this.typeHandler().membersPersistedLengthMaximum();

		// kept and new header values
		final long entityTotalLength = Binary.entityTotalLength(entityContentLength);
		final long entityTypeId      = this.typeHandler().typeId();
		final long entityObjectId    = rawData.getBuildItemObjectId();

		// so funny how they crippled their memory handling API to int just because there is a toArray somewhere.
		final ByteBuffer directByteBuffer = XMemory.allocateDirectNative(entityTotalLength);

		// hardly more than a consistently used and documentable label for the value 0.
		final long entityOffset = 0;

		// replacement binary content is filled and afterwards set as the productive content
		final long targetContentOffset = Binary.toEntityContentOffset(entityOffset);

		// note: DirectByteBuffer instantiation resets all bytes to 0, so no target value "Zeroer" is needed.
		rawData.copyMemory(directByteBuffer, targetContentOffset, this.valueTranslators(), this.targetOffsets());

		// replace the original rawData's content address with the new address, effectively rerouting to the new data
		rawData.modifyLoadItem(directByteBuffer, entityOffset, entityTotalLength, entityTypeId, entityObjectId);

		// registered here to ensure deallocating raw memory at the end of the building process. Neither sooner nor later.
		rawData.registerHelper(directByteBuffer, directByteBuffer);

		// the current type handler can now create a new instance with correctly rearranged raw values
		final T instance = this.typeHandler().create(rawData, handler);

		return instance;
	}

	@Override
	public final void updateState(final Binary rawData, final T instance, final PersistenceLoadHandler handler)
	{
		// rawData is rerouted to the newly allocated memory (handled by a DirectByteBuffer) with rearranged values.
		this.typeHandler().updateState(rawData, instance, handler);
	}

	@Override
	public final void complete(final Binary rawData, final T instance, final PersistenceLoadHandler handler)
	{
		// rawData is rerouted to the newly allocated memory (handled by a DirectByteBuffer) with rearranged values.
		this.typeHandler().complete(rawData, instance, handler);
	}

}
