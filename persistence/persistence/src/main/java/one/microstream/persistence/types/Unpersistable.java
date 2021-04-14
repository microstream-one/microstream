package one.microstream.persistence.types;


/**
 * Marks a type as being unpersistence. Encountering such a type in the dynamic persistence type analysis will cause
 * a validation exception.<br>
 * This type is very useful as a safety net to prevent instances of types that may never end up in a
 * persistent context (database or serialized byte strem) from being persisted.
 * <br>
 * The naming (missing "Persistence" prefix) is intentional to support convenience on the application code level.
 * 
 *
 */
public interface Unpersistable
{
	// Marker interface
}
