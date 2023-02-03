package one.microstream.examples.extensionwrapper;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.exceptions.PersistenceExceptionTransfer;
import one.microstream.persistence.types.PersistenceTarget;

/**
 * Wrapper for {@link PersistenceTarget}, used as base for extensions
 *
 */
public class PersistenceTargetWrapper implements PersistenceTarget<Binary>
{
	private final PersistenceTarget<Binary> delegate;

	public PersistenceTargetWrapper(final PersistenceTarget<Binary> delegate)
	{
		super();
		this.delegate = delegate;
	}

	@Override
	public boolean isWritable()
	{
		return this.delegate.isWritable();
	}

	@Override
	public void write(final Binary data) throws PersistenceExceptionTransfer
	{
		this.delegate.write(data);
	}
	
	
}
