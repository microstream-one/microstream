package one.microstream.persistence.binary.java.util;

import static one.microstream.X.notNull;

import java.util.Queue;

import one.microstream.exceptions.NoSuchMethodRuntimeException;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.reflect.XReflect;


public class BinaryHandlerGenericQueue<T extends Queue<?>> extends AbstractBinaryHandlerQueue<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static final <T extends Queue<?>> BinaryHandlerGenericQueue<T> New(
		final Class<T> type
	)
		throws NoSuchMethodRuntimeException
	{
		final one.microstream.functional.Instantiator<T> instantiator = XReflect.WrapDefaultConstructor(type);
		
		return New(type, l ->
			instantiator.instantiate()
		);
	}
	
	public static final <T extends Queue<?>> BinaryHandlerGenericQueue<T> New(
		final Class<T>        type,
		final Instantiator<T> instantiator
	)
	{
		return new BinaryHandlerGenericQueue<>(
			notNull(type),
			notNull(instantiator)
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final Instantiator<T> instantiator;
	
	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected BinaryHandlerGenericQueue(final Class<T> type, final Instantiator<T> instantiator)
	{
		super(type);
		this.instantiator = notNull(instantiator);
	}


	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public T create(final Binary data, final PersistenceLoadHandler handler)
	{
		return this.instantiator.instantiateQueue(
			getElementCount(data)
		);
	}
	
	
	
	public interface Instantiator<T extends Queue<?>>
	{
		public T instantiateQueue(long elementCount);
	}

}
