package net.jadoth.persistence.types;

/**
 * This type extends the {@link PersistenceTypeHandler} type only by the following reflection contract:<p>
 * A class implementing this type can use arbitrary logic to translate instances of the handled type to
 * their persistent form and back.
 *
 * @author Thomas Muenz
 * @param <M>
 * @param <T>
 * 
 * @see PersistenceTypeHandlerGeneric
 */
public interface PersistenceTypeHandlerCustom<M, T> extends PersistenceTypeHandler<M, T>
{
	// typing interface only (so far)
}
