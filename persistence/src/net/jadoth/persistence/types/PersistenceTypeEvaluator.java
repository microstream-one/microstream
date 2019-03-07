package net.jadoth.persistence.types;

import java.util.function.Predicate;

public interface PersistenceTypeEvaluator extends Predicate<Class<?>>
{
	@Override
	public boolean test(Class<?> type);
}
