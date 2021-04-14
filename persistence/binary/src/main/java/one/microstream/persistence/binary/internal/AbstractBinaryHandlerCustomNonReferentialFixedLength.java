package one.microstream.persistence.binary.internal;

import one.microstream.collections.types.XGettingSequence;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;


public abstract class AbstractBinaryHandlerCustomNonReferentialFixedLength<T>
extends AbstractBinaryHandlerCustomNonReferential<T>
{

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	protected AbstractBinaryHandlerCustomNonReferentialFixedLength(final Class<T> type)
	{
		super(type);
	}

	protected AbstractBinaryHandlerCustomNonReferentialFixedLength(
		final Class<T>                                                    type   ,
		final XGettingSequence<? extends PersistenceTypeDefinitionMember> members
	)
	{
		super(type, members);
	}
	
	protected AbstractBinaryHandlerCustomNonReferentialFixedLength(
		final Class<T>                                                    type    ,
		final String                                                      typeName,
		final XGettingSequence<? extends PersistenceTypeDefinitionMember> members
	)
	{
		super(type, typeName, members);
	}
	
	

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final boolean hasVaryingPersistedLengthInstances()
	{
		return false;
	}

}
