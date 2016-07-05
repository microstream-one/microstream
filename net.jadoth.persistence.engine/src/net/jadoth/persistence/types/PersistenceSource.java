package net.jadoth.persistence.types;

import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.persistence.exceptions.PersistenceExceptionTransfer;
import net.jadoth.swizzling.types.SwizzleIdSet;

public interface PersistenceSource<M>
{
	/**
	 * Read initial data specific to the attached data source.
	 * <p>
	 * Examples:
	 * <ul>
	 * <li>simply ALL data from a simple file.</li>
	 * <li>only root nodes (and all recursive referenced nodes) of a graph-based database.</li>
	 * <li>nothing at all if not applicable.</li>
	 * </ul>
	 *
	 * @return data segments containing all initial data, potentially empty.
	 * @throws PersistenceExceptionTransfer
	 */
	public XGettingCollection<? extends M> readInitial() throws PersistenceExceptionTransfer;

	public XGettingCollection<? extends M> readByObjectIds(SwizzleIdSet[] oids) throws PersistenceExceptionTransfer;

//	public XGettingCollection<? extends M> readByTypeId(long typeId) throws PersistenceExceptionTransfer;

}
