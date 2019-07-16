package one.microstream.java.util;

import static one.microstream.X.notNull;

import java.util.Set;

import one.microstream.exceptions.NoSuchMethodRuntimeException;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.reflect.XReflect;


public class BinaryHandlerGenericSet<T extends Set<?>> extends AbstractBinaryHandlerSet<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static final <T extends Set<?>> BinaryHandlerGenericSet<T> New(
		final Class<T> type
	)
		throws NoSuchMethodRuntimeException
	{
		final one.microstream.functional.Instantiator<T> instantiator = XReflect.WrapDefaultConstructor(type);
		
		return New(type, l ->
			instantiator.instantiate()
		);
	}
	
	public static final <T extends Set<?>> BinaryHandlerGenericSet<T> New(
		final Class<T>        type,
		final Instantiator<T> instantiator
	)
	{
		return new BinaryHandlerGenericSet<>(
			notNull(type),
			notNull(instantiator)
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	public Instantiator<T> instantiator;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryHandlerGenericSet(final Class<T> type, final Instantiator<T> instantiator)
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
