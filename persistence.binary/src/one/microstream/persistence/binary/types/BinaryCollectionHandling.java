package one.microstream.persistence.binary.types;

import one.microstream.collections.XArrays;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustom;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberPseudoField;

public final class BinaryCollectionHandling
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static final XGettingSequence<? extends PersistenceTypeDefinitionMemberPseudoField> sizedArrayPseudoFields(
		final PersistenceTypeDefinitionMemberPseudoField... preHeaderFields)
	{
		return simpleArrayPseudoFields(
			XArrays.add(
				preHeaderFields,
				AbstractBinaryHandlerCustom.pseudoField(long.class, "capacity")
			)
		);
	}

	public static final XGettingSequence<? extends PersistenceTypeDefinitionMemberPseudoField> simpleArrayPseudoFields(
		final PersistenceTypeDefinitionMemberPseudoField... preHeaderFields)
	{
		return AbstractBinaryHandlerCustom.pseudoFields(
			XArrays.add(
				preHeaderFields,
				AbstractBinaryHandlerCustom.complex("elements",
					AbstractBinaryHandlerCustom.pseudoField(Object.class, "element")
				)
			)
		);
	}
	
	public static final XGettingSequence<? extends PersistenceTypeDefinitionMemberPseudoField> keyValuesPseudoFields(
		final PersistenceTypeDefinitionMemberPseudoField... preHeaderFields)
	{
		return AbstractBinaryHandlerCustom.pseudoFields(
			XArrays.add(
				preHeaderFields,
				AbstractBinaryHandlerCustom.complex("elements",
					AbstractBinaryHandlerCustom.pseudoField(Object.class, "key"),
					AbstractBinaryHandlerCustom.pseudoField(Object.class, "value")
				)
			)
		);
	}



	private BinaryCollectionHandling()
	{
		// static only
		throw new UnsupportedOperationException();
	}

}
