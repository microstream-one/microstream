package one.microstream.java.util;

import java.util.Map;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;


public class BinaryHandlerMap<T extends Map<?, ?>> extends AbstractBinaryHandlerMap<T>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	public Instantiator<T> instantiator;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public BinaryHandlerMap(final Class<T> type, final Instantiator<T> instantiator)
	{
		super(type);
		this.instantiator = instantiator;
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public T create(
		final Binary                 bytes  ,
		final PersistenceLoadHandler handler
	)
	{
		return this.instantiator.instantiateMap(
			getElementCount(bytes)
		);
	}
	
	
	
	public interface Instantiator<T extends Map<?, ?>>
	{
		public T instantiateMap(long elementCount);
	}
	
}
