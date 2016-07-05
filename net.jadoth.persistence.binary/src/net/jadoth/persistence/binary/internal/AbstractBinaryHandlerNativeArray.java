package net.jadoth.persistence.binary.internal;

import net.jadoth.collections.X;
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
		final long typeId,
		final Class<A> arrayType,
		final XImmutableSequence<? extends PersistenceTypeDescriptionMemberPseudoField> pseudoFields
	)
	{
		super(typeId, arrayType, pseudoFields);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

//	@Override
//	public final long getFixedBinaryContentLength()
//	{
//		return 0L;
//	}

	@Override
	public boolean isVariableBinaryLengthType()
	{
		return true;
	}

	@Override
	public boolean hasVariableBinaryLengthInstances()
	{
		return false;
	}

}
