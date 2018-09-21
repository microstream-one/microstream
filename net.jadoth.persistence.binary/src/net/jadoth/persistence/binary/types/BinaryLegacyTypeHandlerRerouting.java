package net.jadoth.persistence.binary.types;

import static net.jadoth.X.notNull;

import java.nio.ByteBuffer;

import net.jadoth.X;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.low.XVM;
import net.jadoth.persistence.types.PersistenceTypeDefinition;
import net.jadoth.persistence.types.PersistenceTypeHandler;
import net.jadoth.swizzling.types.SwizzleBuildLinker;

public final class BinaryLegacyTypeHandlerRerouting<T>
extends AbstractBinaryLegacyTypeHandlerTranslating<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static <T> BinaryLegacyTypeHandlerRerouting<T> New(
		final PersistenceTypeDefinition<T>           typeDefinition              ,
		final PersistenceTypeHandler<Binary, T>      typeHandler                 ,
		final XGettingTable<BinaryValueSetter, Long> translatorsWithTargetOffsets
	)
	{
		return new BinaryLegacyTypeHandlerRerouting<>(
			notNull(typeDefinition)                      ,
			notNull(typeHandler)                         ,
			toTranslators(translatorsWithTargetOffsets)  ,
			toTargetOffsets(translatorsWithTargetOffsets)
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryLegacyTypeHandlerRerouting(
		final PersistenceTypeDefinition<T>      typeDefinition  ,
		final PersistenceTypeHandler<Binary, T> typeHandler     ,
		final BinaryValueSetter[]               valueTranslators,
		final long[]                            targetOffsets
	)
	{
		super(typeDefinition, typeHandler, valueTranslators, targetOffsets);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final T create(final Binary rawData)
	{
		final long binaryContentLength = this.typeHandler().membersPersistedLengthMaximum();
		
		// so funny how the morons crippled their memory handling API to int just because there is a toArray somewhere.
		final ByteBuffer directByteBuffer = ByteBuffer.allocateDirect(
			X.checkArrayRange(BinaryPersistence.entityTotalLength(binaryContentLength))
		);
		final long newEntityAddress = XVM.getDirectByteBufferAddress(directByteBuffer);
		
		// header bytes for the mapped format (new length, new TID, same OID) at the newly allocated memory.
		BinaryPersistence.storeEntityHeader(
			newEntityAddress                               ,
			binaryContentLength                            ,
			this.typeHandler().typeId()                    ,
			BinaryPersistence.getBuildItemObjectId(rawData)
		);
		
		// replacement binary content is filled and afterwards set as the productive content
		final long targetContentAddress = BinaryPersistence.entityContentAddress(newEntityAddress);
		
		// note: DirectByteBuffer instantiation resets all bytes to 0, so no target value "Zeroer" is needed.
		final BinaryValueSetter[] translators   = this.valueTranslators();
		final int                 length        = translators.length     ;
		final long[]              targetOffsets = this.targetOffsets()   ;
				
		long srcAddress = rawData.entityContentAddress;
		for(int i = 0; i < length; i++)
		{
			srcAddress = translators[i].setValueToMemory(srcAddress, null, targetContentAddress + targetOffsets[i], null);
		}
		
		rawData.entityContentAddress = targetContentAddress;

		// the current type handler can now create a new instance with correctly rearranged raw values
		final T instance = this.typeHandler().create(rawData);

		// registered to ensure deallocating raw memory at the end of the DBB's life. Neither sooner nor later.
		rawData.setHelper(directByteBuffer);
		
		return instance;
	}

	@Override
	public final void update(final Binary rawData, final T instance, final SwizzleBuildLinker builder)
	{
		// rawData is rerouted to the newly allocated memory (handled by a DirectByteBuffer) with rearranged values.
		this.typeHandler().update(rawData, instance, builder);
	}

	@Override
	public final void complete(final Binary rawData, final T instance, final SwizzleBuildLinker builder)
	{
		// rawData is rerouted to the newly allocated memory (handled by a DirectByteBuffer) with rearranged values.
		this.typeHandler().complete(rawData, instance, builder);
	}
	
}
