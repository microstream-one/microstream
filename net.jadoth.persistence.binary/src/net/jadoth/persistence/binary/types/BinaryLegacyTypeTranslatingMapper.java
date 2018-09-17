package net.jadoth.persistence.binary.types;

import static net.jadoth.X.notNull;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import net.jadoth.X;
import net.jadoth.collections.types.XGettingEnum;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.functional._longProcedure;
import net.jadoth.low.XVM;
import net.jadoth.persistence.types.PersistenceLegacyTypeHandler;
import net.jadoth.persistence.types.PersistenceLegacyTypeMappingResult;
import net.jadoth.persistence.types.PersistenceTypeDefinition;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMember;
import net.jadoth.persistence.types.PersistenceTypeHandler;
import net.jadoth.swizzling.types.SwizzleBuildLinker;
import net.jadoth.swizzling.types.SwizzleFunction;

public final class BinaryLegacyTypeTranslatingMapper<T>
extends PersistenceLegacyTypeHandler.AbstractImplementation<Binary, T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	private static long calculateBinaryContentLength(final PersistenceTypeHandler<Binary, ?> typeHandler)
	{
		long binaryContentLength = 0;
		for(final PersistenceTypeDescriptionMember e : typeHandler.members())
		{
			// returned length values are expected to never be more than 3-digit, so no overflow check needed.
			binaryContentLength += e.persistentMaximumLength();
		}
		
		return binaryContentLength;
	}
	
	public static <T> BinaryLegacyTypeTranslatingMapper<T> New(
		final PersistenceLegacyTypeMappingResult<Binary, T> mappingResult
	)
	{
		final PersistenceTypeDefinition<T>      typeDefinition = mappingResult.legacyTypeDefinition();
		final PersistenceTypeHandler<Binary, T> typeHandler    = mappingResult.currentTypeHandler()  ;
		
		if(typeHandler.hasVaryingPersistedLengthInstances())
		{
			// (14.09.2018 TM)TODO: support VaryingPersistedLengthInstances
			throw new UnsupportedOperationException(
				"Types with instances of varying persisted length are not supported, yet by generic mapping."
			);
		}
		
		final long binaryTotalLength = BinaryPersistence.entityTotalLength(
			calculateBinaryContentLength(typeHandler)
		);
		
		final XGettingSequence<BinaryValueCopier> valueCopiers = deriveValueCopiers(mappingResult);
		
		return new BinaryLegacyTypeTranslatingMapper<>(
			notNull(typeDefinition),
			notNull(typeHandler)   ,
			valueCopiers.toArray(BinaryValueCopier.class),
			X.checkArrayRange(binaryTotalLength)
		);
	}
	
	static XGettingSequence<BinaryValueCopier> deriveValueCopiers(
		final PersistenceLegacyTypeMappingResult<Binary, ?> mappingResult
	)
	{
		// (17.09.2018 TM)FIXME: OGS-3: BinaryLegacyTypeTranslatingMapper#deriveValueCopiers()
		throw new net.jadoth.meta.NotImplementedYetError();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final PersistenceTypeHandler<Binary, T> typeHandler      ;
	private final BinaryValueCopier[]               valueCopiers     ;
	private final int                               binaryTotalLength;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryLegacyTypeTranslatingMapper(
		final PersistenceTypeDefinition<T>      typeDefinition   ,
		final PersistenceTypeHandler<Binary, T> typeHandler      ,
		final BinaryValueCopier[]               valueCopiers     ,
		final int                               binaryTotalLength
	)
	{
		super(typeDefinition);
		this.typeHandler       = typeHandler      ;
		this.valueCopiers      = valueCopiers     ;
		this.binaryTotalLength = binaryTotalLength;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public final XGettingEnum<Field> getInstanceFields()
	{
		return this.typeHandler.getInstanceFields();
	}

	@Override
	public XGettingEnum<Field> getInstancePrimitiveFields()
	{
		return this.typeHandler.getInstancePrimitiveFields();
	}

	@Override
	public XGettingEnum<Field> getInstanceReferenceFields()
	{
		return this.typeHandler.getInstanceReferenceFields();
	}

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
		for(final BinaryValueCopier copier : this.valueCopiers)
		{
			copier.copy(oldEntityContentAddress, newEntityContentAddress);
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
