package one.microstream.persistence.types;

public interface PersistenceTypeEvaluator
{
	public boolean isPersistableType(Class<?> type);
}
