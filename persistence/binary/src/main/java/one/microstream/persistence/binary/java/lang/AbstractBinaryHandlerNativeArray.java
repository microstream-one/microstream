package one.microstream.persistence.binary.java.lang;

import one.microstream.X;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XImmutableSequence;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustom;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberFieldGeneric;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberFieldGenericComplex;


public abstract class AbstractBinaryHandlerNativeArray<A> extends AbstractBinaryHandlerCustom<A>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	protected static final XImmutableSequence<PersistenceTypeDefinitionMemberFieldGenericComplex>
	defineElementsType(final Class<?> componentType)
	{
		// admitted, this is a little crazy. But also very compact.
		return
			X.Constant(
				Complex(
					"elements",
					CustomField(componentType, "element")
				)
			)
		;
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public AbstractBinaryHandlerNativeArray(
		final Class<A>                                                                arrayType   ,
		final XGettingSequence<? extends PersistenceTypeDefinitionMemberFieldGeneric> customFields
	)
	{
		super(arrayType, customFields);
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
