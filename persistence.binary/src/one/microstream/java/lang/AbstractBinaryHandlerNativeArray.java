package one.microstream.java.lang;

import one.microstream.X;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XImmutableSequence;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustom;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberPseudoField;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberPseudoFieldComplex;


public abstract class AbstractBinaryHandlerNativeArray<A> extends AbstractBinaryHandlerCustom<A>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods    //
	/////////////////////

	protected static final XImmutableSequence<PersistenceTypeDefinitionMemberPseudoFieldComplex>
	defineElementsType(final Class<?> componentType)
	{
		// admitted, this is a little crazy. But also very compact.
		return
			X.Constant(
				complex(
					"elements",
					pseudoField(componentType, "element")
				)
			)
		;
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public AbstractBinaryHandlerNativeArray(
		final Class<A>                                                               arrayType   ,
		final XGettingSequence<? extends PersistenceTypeDefinitionMemberPseudoField> pseudoFields
	)
	{
		super(arrayType, pseudoFields);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////
	
	@Override
	public final boolean hasPersistedVariableLength()
	{
		return true;
	}

	@Override
	public final boolean hasVaryingPersistedLengthInstances()
	{
		// note: java array might become truncatable in the future, so instance length might indeed change
		return true;
	}

}
