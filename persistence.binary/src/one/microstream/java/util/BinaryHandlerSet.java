package one.microstream.java.util;

import java.util.Set;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;


public class BinaryHandlerSet<T extends Set<?>> extends AbstractBinaryHandlerSet<T>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	public Instantiator<T> instantiator;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryHandlerSet(final Class<T> type, final Instantiator<T> instantiator)
	{
		super(type);
		this.instantiator = instantiator;
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public T create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		return this.instantiator.instantiateSet(
			getElementCount(bytes)
		);
	}

	
	
	public interface Instantiator<T extends Set<?>>
	{
		public T instantiateSet(long elementCount);
	}

}
