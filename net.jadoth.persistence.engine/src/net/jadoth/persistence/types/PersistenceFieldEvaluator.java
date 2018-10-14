package net.jadoth.persistence.types;

import java.lang.reflect.Field;

/**
 * Alias type to concretely identify the task of evaluating a {@link Field}'s persistability
 *
 * @author TM
 */
@FunctionalInterface
public interface PersistenceFieldEvaluator
{
	public boolean isPersistable(Class<?> entityType, Field field);

}
