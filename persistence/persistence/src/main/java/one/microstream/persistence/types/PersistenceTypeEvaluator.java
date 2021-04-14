package one.microstream.persistence.types;

@FunctionalInterface
public interface PersistenceTypeEvaluator
{
	public boolean isPersistableType(Class<?> type);
}
