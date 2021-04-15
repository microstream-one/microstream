package one.microstream.examples.eagerstoring;

import java.lang.reflect.Field;

import one.microstream.persistence.types.PersistenceEagerStoringFieldEvaluator;

/**
 * Custom field evaluator which looks for the {@link StoreEager} annotation.
 *
 */
public class StoreEagerEvaluator implements PersistenceEagerStoringFieldEvaluator
{

	@Override
	public boolean isEagerStoring(
		final Class<?> clazz,
		final Field    field
	)
	{
		return field.isAnnotationPresent(StoreEager.class);
	}
	
}
