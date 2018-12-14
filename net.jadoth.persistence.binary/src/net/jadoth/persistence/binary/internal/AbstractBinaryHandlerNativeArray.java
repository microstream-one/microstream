package net.jadoth.persistence.binary.internal;

import net.jadoth.X;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.collections.types.XImmutableSequence;
import net.jadoth.persistence.types.PersistenceTypeDefinitionMemberPseudoField;
import net.jadoth.persistence.types.PersistenceTypeDefinitionMemberPseudoFieldComplex;


public abstract class AbstractBinaryHandlerNativeArray<A> extends AbstractBinaryHandlerNativeCustom<A>
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
