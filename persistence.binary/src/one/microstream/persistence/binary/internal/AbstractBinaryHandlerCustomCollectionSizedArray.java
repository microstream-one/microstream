package one.microstream.persistence.binary.internal;

import one.microstream.collections.types.XGettingSequence;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceSizedArrayLengthController;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberPseudoField;


public abstract class AbstractBinaryHandlerCustomCollectionSizedArray<T>
extends AbstractBinaryHandlerCustomCollection<T>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final PersistenceSizedArrayLengthController controller;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AbstractBinaryHandlerCustomCollectionSizedArray(
		final Class<T>                                                               type        ,
		final XGettingSequence<? extends PersistenceTypeDefinitionMemberPseudoField> pseudoFields,
		final PersistenceSizedArrayLengthController                                  controller
	)
	{
		super(type, pseudoFields);
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
