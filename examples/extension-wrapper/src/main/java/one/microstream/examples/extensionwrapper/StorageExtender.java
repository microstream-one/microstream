package one.microstream.examples.extensionwrapper;

import one.microstream.functional.InstanceDispatcherLogic;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceStorer;
import one.microstream.persistence.types.PersistenceTarget;

/**
 * Dispatcher logic which is used to extend certain parts
 */
public class StorageExtender implements InstanceDispatcherLogic
{
	@SuppressWarnings("unchecked")
	@Override
	public <T> T apply(final T subject)
	{
		if(subject instanceof PersistenceTarget)
		{
			return (T)new PersistenceTargetExtension((PersistenceTarget<Binary>)subject);
		}
		
		if(subject instanceof PersistenceStorer.Creator)
		{
			return (T)new PersistenceStorerExtension.Creator((PersistenceStorer.Creator<Binary>)subject);
		}
		
		return subject;
	}
}
