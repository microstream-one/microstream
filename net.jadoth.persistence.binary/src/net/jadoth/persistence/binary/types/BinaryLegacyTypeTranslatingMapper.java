package net.jadoth.persistence.binary.types;

import static net.jadoth.X.notNull;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import net.jadoth.X;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.functional._longProcedure;
import net.jadoth.low.XVM;
import net.jadoth.persistence.types.PersistenceLegacyTypeHandler;
import net.jadoth.persistence.types.PersistenceTypeDefinition;
import net.jadoth.persistence.types.PersistenceTypeHandler;
import net.jadoth.swizzling.types.SwizzleBuildLinker;
import net.jadoth.swizzling.types.SwizzleFunction;

public final class BinaryLegacyTypeTranslatingMapper<T>
extends PersistenceLegacyTypeHandler.AbstractImplementation<Binary, T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static <T> BinaryLegacyTypeTranslatingMapper<T> New(
		final PersistenceTypeDefinition<T>            typeDefinition   ,
		final PersistenceTypeHandler<Binary, T>       typeHandler      ,
		final XGettingSequence<BinaryValueTranslator> valueTranslators ,
		final long                                    binaryTotalLength
	)
	{
		return new BinaryLegacyTypeTranslatingMapper<>(
			notNull(typeDefinition),
			notNull(typeHandler)   ,
			valueTranslators.toArray(BinaryValueTranslator.class),
			X.checkArrayRange(binaryTotalLength)
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final PersistenceTypeHandler<Binary, T> typeHandler      ;
	private final BinaryValueTranslator[]           valueTranslators ;
	private final int                               binaryTotalLength;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryLegacyTypeTranslatingMapper(
		final PersistenceTypeDefinition<T>      typeDefinition   ,
		final PersistenceTypeHandler<Binary, T> typeHandler      ,
		final BinaryValueTranslator[]           valueTranslators ,
		final int                               binaryTotalLength
	)
	{
		super(typeDefinition);
		this.typeHandler       = typeHandler      ;
		this.valueTranslators  = valueTranslators ;
		this.binaryTotalLength = binaryTotalLength;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public boolean hasInstanceReferences()
	{
		return this.typeHandler.hasInstanceReferences();
	}

	@Override
	public void iterateInstanceReferences(final T instance, final SwizzleFunction iterator)
	{
		this.typeHandler.iterateInstanceReferences(instance, iterator);
	}

	@Override
	public void iteratePersistedReferences(final Binary rawData, final _longProcedure iterator)
	{
		this.typeHandler.iteratePersistedReferences(rawData, iterator);
	}
	
	@Override
	public <C extends Consumer<? super Class<?>>> C iterateMemberTypes(final C logic)
	{
		return this.typeHandler.iterateMemberTypes(logic);
	}

	@Override
	public final T create(final Binary rawData)
	{
		// so funny how the morons crippled their memory handling API to int just because there is a toArray somewhere.
		final ByteBuffer directByteBuffer = ByteBuffer.allocateDirect(this.binaryTotalLength);
		
		// more JDK moronity
		final long newEntityAddress = XVM.getDirectByteBufferAddress(directByteBuffer);
		
		// header bytes for the mapped format (new length, new TID, same OID) at the newly allocated memory.
		BinaryPersistence.storeEntityHeader(
			newEntityAddress                                             ,
			BinaryPersistence.entityContentLength(this.binaryTotalLength),
			this.typeHandler.typeId()                                    ,
			BinaryPersistence.getBuildItemObjectId(rawData)
		);
		
		final long oldEntityContentAddress = rawData.entityContentAddress;
		final long newEntityContentAddress = BinaryPersistence.entityContentAddress(newEntityAddress);
		
		// note: DirectByteBuffer instantiation does already reset all bytes to 0, so not "Zeroer" is needed.
		for(final BinaryValueTranslator translator : this.valueTranslators)
		{
			translator.translateValue(oldEntityContentAddress, newEntityContentAddress);
		}

		// set newEntityContentAddress as entityContentAddress for later use in update()
		rawData.entityContentAddress = newEntityContentAddress;

		// the current type handler can now create a new instance with correctly rearranged raw values
		final T instance = this.typeHandler.create(rawData);

		// registered to ensure deallocating raw memory at the end of the DBB's life. Neither sooner nor later.
		rawData.setHelper(directByteBuffer);
		
		return instance;
	}

	@Override
	public final void update(final Binary rawData, final T instance, final SwizzleBuildLinker builder)
	{
		// rawData is rerouted to the newly allocated memory (handled by a DirectByteBuffer) with rearranged values.
		this.typeHandler.update(rawData, instance, builder);
	}

	@Override
	public final void complete(final Binary rawData, final T instance, final SwizzleBuildLinker builder)
	{
		// rawData is rerouted to the newly allocated memory (handled by a DirectByteBuffer) with rearranged values.
		this.typeHandler.complete(rawData, instance, builder);
		
		rawData.setHelper(null); // might help ease Garbage Collection
	}
	
}
