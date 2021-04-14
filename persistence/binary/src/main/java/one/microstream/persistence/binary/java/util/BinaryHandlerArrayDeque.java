package one.microstream.persistence.binary.java.util;

import java.util.ArrayDeque;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;


/*
 * Since there is no way of ensuring capacity in the once again hilariously bad JDK code that is the ArrayDeque
 * (aside from setting an externally created array) AND I couldn't care less about that weird collection type in
 * the first place, the ArrayDeque is, after long attempts of implementing it efficiently, hereby handled generically.
 * On any complaints, write a custom type handler and use that.
 */
public final class BinaryHandlerArrayDeque extends AbstractBinaryHandlerQueue<ArrayDeque<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<ArrayDeque<?>> handledType()
	{
		return (Class)ArrayDeque.class; // no idea how to get ".class" to work otherwise
	}
	
	public static BinaryHandlerArrayDeque New()
	{
		return new BinaryHandlerArrayDeque();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerArrayDeque()
	{
		super(
			handledType()
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public ArrayDeque<?> create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new ArrayDeque<>();
	}
	
}
