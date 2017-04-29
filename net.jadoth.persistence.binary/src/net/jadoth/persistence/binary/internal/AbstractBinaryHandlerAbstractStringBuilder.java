package net.jadoth.persistence.binary.internal;



public abstract class AbstractBinaryHandlerAbstractStringBuilder<B/*extends AbstractStringBuilder*/>
extends AbstractBinaryHandlerNativeCustom<B>
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	private   static final int  BITS_3        = 3                      ;
	protected static final long LENGTH_LENGTH = Integer.SIZE >>> BITS_3;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////
	
	protected AbstractBinaryHandlerAbstractStringBuilder(final Class<B> type)
	{
		super(type, pseudoFields(
			pseudoField(long.class, "capacity"),
			chars("value")
		));
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	// sadly, a common abstract handling logic can't be done because AbstractStringBuilder is not public (geniuses)

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
	
	@Override
	public final boolean hasPersistedVariableLength()
	{
		return true;
	}

	@Override
	public final boolean hasVaryingPersistedLengthInstances()
	{
		return false;
	}

}
