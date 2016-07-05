package net.jadoth.persistence.types;



/**
 * This type extends the {@link PersistenceTypeHandler} type only by the following reflection contract:<p>
 *
 * <ul>
 * <li>An implemention implementing this type must solely handle actual class fields and may not use
 * any custom persistent state like e.g. {@link PersistenceTypeHandlerCustom}</li>
 * </ul>
 *
 * @author Thomas Muenz
 * @param <M>
 * @param <T>
 */
public interface PersistenceTypeHandlerGeneric<M, T> extends PersistenceTypeHandler<M, T>
{
	// typing interface only (so far)
}
