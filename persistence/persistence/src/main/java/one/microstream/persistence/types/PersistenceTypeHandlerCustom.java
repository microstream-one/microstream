package one.microstream.persistence.types;

/**
 * This type extends the {@link PersistenceTypeHandler} type only by the following reflection contract:<p>
 * A class implementing this type can use arbitrary logic to translate instances of the handled type to
 * their persistent form and back.
 *
 * 
 * @param <D>
 * @param <T>
 * 
 * @see PersistenceTypeHandlerGeneric
 */
public interface PersistenceTypeHandlerCustom<D, T> extends PersistenceTypeHandler<D, T>
{
	// typing interface only (so far)
}
