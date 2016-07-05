package net.jadoth.persistence.types;

import java.lang.reflect.Field;
import java.util.function.Predicate;

/**
 * Alias type to concretely identify the task of evaluating a {@link Field}'s persistability
 *
 * @author TM
 */
public interface PersistenceFieldEvaluator extends Predicate<Field>
{
	@Override
	public boolean test(Field field);

}
