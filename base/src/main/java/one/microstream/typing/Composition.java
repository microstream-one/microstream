package one.microstream.typing;

/**
 * Marker type to indicate that a certain implementation is a composition (is composed of unshared objects).
 * This is usefull (or even necessary) for having a means of distinguishing generically handlable implementations
 * from implementations that require (or at least suggest) tailored generic treatment, e.g. for a persistence layer
 * to persist the unshared objects in an inlined fashion rather than storing an external reference.
 * <p>
 * This is done via an interface instead of an annotations because the design aspect to be represented is a typical
 * "is-a" relation and because annotation should actually not alter programm behavior.
 *
 * @see ComponentType
 *
 * 
 */
public interface Composition
{
	// Marker interface
}
