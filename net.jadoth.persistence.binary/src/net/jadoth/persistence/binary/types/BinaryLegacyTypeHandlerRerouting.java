package net.jadoth.persistence.binary.types;

import static net.jadoth.X.mayNull;
import static net.jadoth.X.notNull;

import java.nio.ByteBuffer;

import net.jadoth.X;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.memory.XMemory;
import net.jadoth.persistence.types.PersistenceLegacyTypeHandlingListener;
import net.jadoth.persistence.types.PersistenceLoadHandler;
import net.jadoth.persistence.types.PersistenceTypeDefinition;
import net.jadoth.persistence.types.PersistenceTypeHandler;

public final class BinaryLegacyTypeHandlerRerouting<T>
extends AbstractBinaryLegacyTypeHandlerTranslating<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static <T> BinaryLegacyTypeHandlerRerouting<T> New(
		final PersistenceTypeDefinition                     typeDefinition              ,
		final PersistenceTypeHandler<Binary, T>             typeHandler                 ,
		final XGettingTable<BinaryValueSetter, Long>        translatorsWithTargetOffsets,
		final PersistenceLegacyTypeHandlingListener<Binary> listener
	)
	{
		return new BinaryLegacyTypeHandlerRerouting<>(
			notNull(typeDefinition)                      ,
			notNull(typeHandler)                         ,
			toTranslators(translatorsWithTargetOffsets)  ,
			toTargetOffsets(translatorsWithTargetOffsets),
			mayNull(listener)
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
		final PersistenceLegacyTypeHandlingListener<Binary> listener
	)
	{
		super(typeDefinition, typeHandler, valueTranslators, targetOffsets, listener);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	protected T internalCreate(final Binary rawData)
	{
		final long entityContentLength = this.typeHandler().membersPersistedLengthMaximum();
		final long entityTotalLength   = Binary.entityTotalLength(entityContentLength);
		
		// so funny how the morons crippled their memory handling API to int just because there is a toArray somewhere.
		final ByteBuffer directByteBuffer = ByteBuffer.allocateDirect(
			X.checkArrayRange(entityTotalLength)
		);
		final long newEntityAddress = XMemory.getDirectByteBufferAddress(directByteBuffer);
		
		// header bytes for the mapped format (new length, new TID, same OID) at the newly allocated memory.
		Binary.setEntityHeaderRawValues(
			newEntityAddress              ,
			entityTotalLength             ,
			this.typeHandler().typeId()   ,
			rawData.getBuildItemObjectId()
		);
		
		// replacement binary content is filled and afterwards set as the productive content
		final long targetContentAddress = Binary.entityContentAddress(newEntityAddress);
		
		// note: DirectByteBuffer instantiation resets all bytes to 0, so no target value "Zeroer" is needed.
		final BinaryValueSetter[] translators   = this.valueTranslators();
		final int                 length        = translators.length     ;
		final long[]              targetOffsets = this.targetOffsets()   ;
				
		long srcAddress = rawData.loadItemEntityContentAddress();
		for(int i = 0; i < length; i++)
		{
			srcAddress = translators[i].setValueToMemory(srcAddress, null, targetContentAddress + targetOffsets[i], null);
		}
		
		// replace the original rawData's content address with the new address, effectively rerouting to the new data
		rawData.setLoadItemEntityContentAddress(targetContentAddress);

		// the current type handler can now create a new instance with correctly rearranged raw values
		final T instance = this.typeHandler().create(rawData);

		// registered here to ensure deallocating raw memory at the end of the building process. Neither sooner nor later.
		rawData.anchorHelper(directByteBuffer);
		
		return instance;
	}

	@Override
	public final void update(final Binary rawData, final T instance, final PersistenceLoadHandler builder)
	{
		// rawData is rerouted to the newly allocated memory (handled by a DirectByteBuffer) with rearranged values.
		this.typeHandler().update(rawData, instance, builder);
	}

	@Override
	public final void complete(final Binary rawData, final T instance, final PersistenceLoadHandler builder)
	{
		// rawData is rerouted to the newly allocated memory (handled by a DirectByteBuffer) with rearranged values.
		this.typeHandler().complete(rawData, instance, builder);
	}
	
}
