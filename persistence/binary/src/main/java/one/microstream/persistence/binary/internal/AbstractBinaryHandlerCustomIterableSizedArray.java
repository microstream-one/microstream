package one.microstream.persistence.binary.internal;

import one.microstream.collections.types.XGettingSequence;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceSizedArrayLengthController;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberFieldGeneric;


public abstract class AbstractBinaryHandlerCustomIterableSizedArray<T extends Iterable<?>>
extends AbstractBinaryHandlerCustomIterable<T>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final PersistenceSizedArrayLengthController controller;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AbstractBinaryHandlerCustomIterableSizedArray(
		final Class<T>                                                                type        ,
		final XGettingSequence<? extends PersistenceTypeDefinitionMemberFieldGeneric> customFields,
		final PersistenceSizedArrayLengthController                                   controller
	)
	{
		super(type, customFields);
		this.controller = controller;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	protected final int determineArrayLength(final Binary data, final long sizedArrayOffset)
	{
		final int specifiedLength      = data.getSizedArrayLength(sizedArrayOffset);
		final int actualElementCount   = data.getSizedArrayElementCount(sizedArrayOffset);
		final int effectiveArrayLength = this.controller.controlArrayLength(specifiedLength, actualElementCount);
		
		return effectiveArrayLength;
	}

}
