package net.jadoth.persistence.types;


/**
 * (So far only a stub for the) conceptual type and ID consistency area.
 * All runtimes belonging to a specific domain must fulfill the following conditions:
 * - May not have different type definitions (but must not necessarily know all types of all other runtimes)
 * - May not assign object ids that collide with assignments from other runtimes
 * - Must have the same type ids associated with full qualified type names
 * - Must have the same constant ids associated with full qualified reference constant field names
 *
 * A persistence domain has exactely one type server runtime (managing type id assignment)
 * and one master object id server runtime (which may be or typically is, but does not have to be
 * both in the same runtime)
 *
 * @author Thomas Muenz
 */
public interface PersistenceDomain
{
	// empty stub so far
}
