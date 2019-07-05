package one.microstream.java.util;

import java.util.List;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;


public class BinaryHandlerList<T extends List<?>> extends AbstractBinaryHandlerList<T>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	public Instantiator<T> instantiator;
	
	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryHandlerList(final Class<T> type, final Instantiator<T> instantiator)
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
		return this.instantiator.instantiateList(
			getElementCount(bytes)
		);
	}

	
	
	public interface Instantiator<T extends List<?>>
	{
		public T instantiateList(long elementCount);
	}

}
