package net.jadoth.persistence.types;

import java.lang.reflect.Field;
import java.util.function.Predicate;

@FunctionalInterface
public interface PersistenceReferenceFieldMandatoryEvaluator extends Predicate<Field>
{
	@Override
	public boolean test(Field field);

}