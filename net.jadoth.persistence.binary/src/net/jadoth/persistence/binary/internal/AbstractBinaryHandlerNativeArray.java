package net.jadoth.persistence.binary.internal;

import net.jadoth.X;
import net.jadoth.collections.types.XImmutableSequence;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMemberPseudoField;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMemberPseudoFieldComplex;


public abstract class AbstractBinaryHandlerNativeArray<A> extends AbstractBinaryHandlerNativeCustom<A>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods    //
	/////////////////////

	protected static final XImmutableSequence<PersistenceTypeDescriptionMemberPseudoFieldComplex>
	defineElementsType(final Class<?> componentType)
	{
		// admitted, this is a little crazy :D. But also very compact, if you think about it.
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
		final Class<A>                                                                  arrayType   ,
		final XImmutableSequence<? extends PersistenceTypeDescriptionMemberPseudoField> pseudoFields
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
