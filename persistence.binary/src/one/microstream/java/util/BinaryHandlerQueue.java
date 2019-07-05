package one.microstream.java.util;

import java.util.Queue;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;


public class BinaryHandlerQueue<T extends Queue<?>> extends AbstractBinaryHandlerQueue<T>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	public Instantiator<T> instantiator;
	
	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryHandlerQueue(final Class<T> type, final Instantiator<T> instantiator)
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
		return this.instantiator.instantiateQueue(
			getElementCount(bytes)
		);
	}
	
	
	
	public interface Instantiator<T extends Queue<?>>
	{
		public T instantiateQueue(long elementCount);
	}

}
