package one.microstream.persistence.types;

import java.lang.reflect.Field;

/**
 * This type extends the {@link PersistenceTypeHandler} type only by the following reflection contract:
 * <p>
 * A class implementing this type must solely handle actual class {@link Field}s and their values.
 * It may not use any custom persistent state or logic like e.g. {@link PersistenceTypeHandlerCustom}.
 *
 * 
 * @param <D>
 * @param <T>
 * 
 * @see PersistenceTypeHandlerCustom
 */
public interface PersistenceTypeHandlerGeneric<D, T> extends PersistenceTypeHandler<D, T>
{
	// typing interface only (so far)
}
