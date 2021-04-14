package one.microstream.persistence.binary.java.util;

import static one.microstream.X.notNull;

import java.util.Map;

import one.microstream.exceptions.NoSuchMethodRuntimeException;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.reflect.XReflect;


public class BinaryHandlerGenericMap<T extends Map<?, ?>> extends AbstractBinaryHandlerMap<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static final <T extends Map<?, ?>> BinaryHandlerGenericMap<T> New(
		final Class<T> type
	)
		throws NoSuchMethodRuntimeException
	{
		final one.microstream.functional.Instantiator<T> instantiator = XReflect.WrapDefaultConstructor(type);
		
		return New(type, l ->
			instantiator.instantiate()
		);
	}
	
	public static final <T extends Map<?, ?>> BinaryHandlerGenericMap<T> New(
		final Class<T>        type,
		final Instantiator<T> instantiator
	)
	{
		return new BinaryHandlerGenericMap<>(
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
	
	protected BinaryHandlerGenericMap(final Class<T> type, final Instantiator<T> instantiator)
	{
		super(type);
		this.instantiator = notNull(instantiator);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public T create(
		final Binary                 data   ,
		final PersistenceLoadHandler handler
	)
	{
		return this.instantiator.instantiateMap(
			getElementCount(data)
		);
	}
	
	
	
	public interface Instantiator<T extends Map<?, ?>>
	{
		public T instantiateMap(long elementCount);
	}
	
}
