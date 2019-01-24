package net.jadoth.persistence.binary.internal;

import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryCollectionHandling;
import net.jadoth.persistence.types.PersistenceSizedArrayLengthController;
import net.jadoth.persistence.types.PersistenceTypeDefinitionMemberPseudoField;


public abstract class AbstractBinaryHandlerNativeCustomCollectionSizedArray<T>
extends AbstractBinaryHandlerNativeCustomCollection<T>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final PersistenceSizedArrayLengthController controller;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public AbstractBinaryHandlerNativeCustomCollectionSizedArray(
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
		final int specifiedLength      = BinaryCollectionHandling.getSizedArrayLength(data, sizedArrayOffset);
		final int actualElementCount   = BinaryCollectionHandling.getSizedArrayElementCount(data, sizedArrayOffset);
		final int effectiveArrayLength = this.controller.controlArrayLength(specifiedLength, actualElementCount);
		
		return effectiveArrayLength;
	}

}
