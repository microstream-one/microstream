package one.microstream.entity;

/**
 * Immutable entities effectively never change their data as viewed from an outside context.
 * 
 * @author TM
 *
 * @param <E>
 */
public interface ImmutableEntity<E extends ImmutableEntity<E>> extends ReadableEntity<E>
{
	// so far only a typing interface to define a more specific contract
}
