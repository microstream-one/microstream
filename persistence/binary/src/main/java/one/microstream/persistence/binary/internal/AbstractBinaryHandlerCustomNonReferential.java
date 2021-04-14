package one.microstream.persistence.binary.internal;

import one.microstream.collections.types.XGettingSequence;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;


/**
 * Handler for types that are mutable but have no references. E.g. {@link java.util.Date}.
 * 
 * 
 *
 * @param <T>
 */
public abstract class AbstractBinaryHandlerCustomNonReferential<T>
extends AbstractBinaryHandlerCustom<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	protected AbstractBinaryHandlerCustomNonReferential(final Class<T> type)
	{
		super(type);
	}

	protected AbstractBinaryHandlerCustomNonReferential(
		final Class<T>                                                    type   ,
		final XGettingSequence<? extends PersistenceTypeDefinitionMember> members
	)
	{
		super(type, members);
	}
	
	protected AbstractBinaryHandlerCustomNonReferential(
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
	public final void iterateLoadableReferences(final Binary data, final PersistenceReferenceLoader iterator)
	{
		// no-op
	}
	
	@Override
	public final boolean hasPersistedReferences()
	{
		return false;
	}

}
