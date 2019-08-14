package one.microstream.persistence.types;

import java.io.File;

import one.microstream.collections.types.XGettingCollection;
import one.microstream.persistence.exceptions.PersistenceExceptionTransfer;

public interface PersistenceSource<M>
{
	/**
	 * A general, unspecific read, e.g. to initially read data in general from the attached data source.
	 * <p>
	 * Examples:
	 * <ul>
	 * <li>simply ALL data from a plain file.</li>
	 * <li>only root nodes (and all recursively referenced nodes) of a graph-based database.</li>
	 * <li>nothing at all if not applicable, resulting in {@code null} being returned.</li>
	 * </ul>
	 *
	 * @return data segments containing general data if applicable, otherwise {@code null}.
	 * @throws PersistenceExceptionTransfer
	 */
	public XGettingCollection<? extends M> read() throws PersistenceExceptionTransfer;

	public XGettingCollection<? extends M> readByObjectIds(PersistenceIdSet[] oids) throws PersistenceExceptionTransfer;
	
	/**
	 * Prepare to read from this source. E.g. open a defined {@link File}.
	 * 
	 */
	public default void prepareSource()
	{
		// no-op by default.
	}
	
	/**
	 * Take actions to deactivate/close/destroy the source because it won't be read again.
	 */
	public default void closeSource()
	{
		// no-op by default.
	}

}
