package one.microstream.java.util;

import java.util.ArrayDeque;


/*
 * Since there is no way of ensuring capacity in the once again hilariously bad JDK code that is the ArrayDeque
 * (aside from setting an externally created array) AND I couldn't care less about that weird collection type in
 * the first place, the ArrayDeque is, after long attempts of implementing it efficiently, hereby handled generically.
 * On any complaints, write a custom type handler and use that.
 */
public final class BinaryHandlerArrayDeque extends BinaryHandlerQueue<ArrayDeque<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<ArrayDeque<?>> typeWorkaround()
	{
		return (Class)ArrayDeque.class; // no idea how to get ".class" to work otherwise
	}
	
	public static final ArrayDeque<?> instantiate(final long elementCount)
	{
		return new ArrayDeque<>();
	}


	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryHandlerArrayDeque()
	{
		super(
			typeWorkaround(),
			BinaryHandlerArrayDeque::instantiate
		);
	}
	
}