package net.jadoth.persistence.types;

public interface PersistenceTypeLink extends PersistenceTypeIdOwner
{
	@Override
	public long     typeId();

	public Class<?> type();

}
