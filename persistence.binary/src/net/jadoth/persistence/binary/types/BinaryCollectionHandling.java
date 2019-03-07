package net.jadoth.persistence.binary.types;

import net.jadoth.collections.XArrays;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.persistence.binary.internal.AbstractBinaryHandlerNative;
import net.jadoth.persistence.types.PersistenceTypeDefinitionMemberPseudoField;

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
