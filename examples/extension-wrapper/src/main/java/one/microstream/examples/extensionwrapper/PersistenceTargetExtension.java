package one.microstream.examples.extensionwrapper;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.exceptions.PersistenceExceptionTransfer;
import one.microstream.persistence.types.PersistenceTarget;

/**
 * Extension for {@link PersistenceTarget} which adds logic to write operations
 *
 */
public class PersistenceTargetExtension extends PersistenceTargetWrapper
{
	public PersistenceTargetExtension(final PersistenceTarget<Binary> delegate)
	{
		super(delegate);
	}


	@Override
	public void write(final Binary data) throws PersistenceExceptionTransfer
	{
		// Original write
		super.write(data);
		
		// Add extension code
		System.out.println("Data written");
	}
	
	
}
