package net.jadoth.persistence.binary.internal;

import net.jadoth.collections.types.XImmutableSequence;
import net.jadoth.functional._longProcedure;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMemberPseudoField;
import net.jadoth.swizzling.types.SwizzleFunction;

public abstract class AbstractBinaryHandlerNativeArrayPrimitive<A> extends AbstractBinaryHandlerNativeArray<A>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////
	
	public AbstractBinaryHandlerNativeArrayPrimitive(
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
	public final void iterateInstanceReferences(final A instance, final SwizzleFunction iterator)
	{
		// no references to iterate in arrays with primitive component type
	}

	@Override
	public final void iteratePersistedReferences(final Binary offset, final _longProcedure iterator)
	{
		// no references to iterate in arrays with primitive component type
	}

	@Override
	public final boolean hasInstanceReferences()
	{
		return false;
	}
	
	@Override
	public final boolean hasPersistedReferences()
	{
		return false;
	}
	
}
