package one.microstream.persistence.types;

import java.util.function.Predicate;

public interface PersistenceTypeEvaluator extends Predicate<Class<?>>
{
	@Override
	public boolean test(Class<?> type);
}
