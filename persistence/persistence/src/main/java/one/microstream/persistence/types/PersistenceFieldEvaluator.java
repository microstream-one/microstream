package one.microstream.persistence.types;

import java.lang.reflect.Field;

/**
 * Alias type to concretely identify the task of evaluating a {@link Field}'s persistability
 *
 * 
 */
@FunctionalInterface
public interface PersistenceFieldEvaluator
{
	public boolean applies(Class<?> entityType, Field field);

}
