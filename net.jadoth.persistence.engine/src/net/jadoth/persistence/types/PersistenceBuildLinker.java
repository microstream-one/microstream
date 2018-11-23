package net.jadoth.persistence.types;

public interface PersistenceBuildLinker extends PersistenceObjectIdResolving
{
	public PersistenceObjectSupplier getSwizzleObjectSupplier();
}
