package one.microstream.persistence.binary.types;

import one.microstream.collections.XArrays;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerNative;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberPseudoField;

public final class BinaryCollectionHandling
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static final XGettingSequence<? extends PersistenceTypeDefinitionMemberPseudoField> sizedArrayPseudoFields(
		final PersistenceTypeDefinitionMemberPseudoField... preHeaderFields)
	{
		return elementsPseudoFields(
			XArrays.add(
				preHeaderFields,
				AbstractBinaryHandlerNative.pseudoField(long.class, "capacity")
			)
		);
	}

	public static final XGettingSequence<? extends PersistenceTypeDefinitionMemberPseudoField> elementsPseudoFields(
		final PersistenceTypeDefinitionMemberPseudoField... preHeaderFields)
	{
		return AbstractBinaryHandlerNative.pseudoFields(
			XArrays.add(
				preHeaderFields,
				AbstractBinaryHandlerNative.complex("elements",
					AbstractBinaryHandlerNative.pseudoField(Object.class, "element")
				)
			)
		);
	}

	public static final XGettingSequence<? extends PersistenceTypeDefinitionMemberPseudoField> simpleArrayPseudoFields(
		final PersistenceTypeDefinitionMemberPseudoField... preHeaderFields)
	{
		return AbstractBinaryHandlerNative.pseudoFields(
			XArrays.add(
				preHeaderFields,
				AbstractBinaryHandlerNative.complex("elements",
					AbstractBinaryHandlerNative.pseudoField(Object.class, "element")
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
