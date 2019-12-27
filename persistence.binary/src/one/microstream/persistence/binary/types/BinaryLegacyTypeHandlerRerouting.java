package one.microstream.persistence.binary.types;

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;

import java.nio.ByteBuffer;

import one.microstream.memory.XMemory;
import one.microstream.collections.types.XGettingTable;
import one.microstream.persistence.binary.internal.AbstractBinaryLegacyTypeHandlerTranslating;
import one.microstream.persistence.types.PersistenceLegacyTypeHandlingListener;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeHandler;

public final class BinaryLegacyTypeHandlerRerouting<T>
extends AbstractBinaryLegacyTypeHandlerTranslating<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static <T> BinaryLegacyTypeHandlerRerouting<T> New(
		final PersistenceTypeDefinition                     typeDefinition              ,
		final PersistenceTypeHandler<Binary, T>             typeHandler                 ,
		final XGettingTable<Long, BinaryValueSetter>        translatorsWithTargetOffsets,
		final PersistenceLegacyTypeHandlingListener<Binary> listener                    ,
		final boolean                                       switchByteOrder
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
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	protected T internalCreate(final Binary rawData, final PersistenceLoadHandler handler)
	{
		final long entityContentLength = this.typeHandler().membersPersistedLengthMaximum();
		
		// kept and new header values
		final long entityTotalLength = Binary.entityTotalLength(entityContentLength);
		final long entityTypeId      = this.typeHandler().typeId();
		final long entityObjectId    = rawData.getBuildItemObjectId();
		
		// so funny how the morons crippled their memory handling API to int just because there is a toArray somewhere.
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
	public final void update(final Binary rawData, final T instance, final PersistenceLoadHandler handler)
	{
		// rawData is rerouted to the newly allocated memory (handled by a DirectByteBuffer) with rearranged values.
		this.typeHandler().update(rawData, instance, handler);
	}

	@Override
	public final void complete(final Binary rawData, final T instance, final PersistenceLoadHandler handler)
	{
		// rawData is rerouted to the newly allocated memory (handled by a DirectByteBuffer) with rearranged values.
		this.typeHandler().complete(rawData, instance, handler);
	}
	
}
