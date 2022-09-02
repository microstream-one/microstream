package one.microstream.integrations.quarkus.deployment.test;

import one.microstream.integrations.quarkus.types.config.StorageManagerInitializer;
import one.microstream.storage.types.StorageManager;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SomeInitializerForStorage implements StorageManagerInitializer
{
    @Override
    public void initialize(StorageManager storageManager)
    {
        SomeRootWithStorage rootObject = (SomeRootWithStorage) storageManager.root();
        if (rootObject == null) {
            throw new IllegalStateException("StorageManager should have already a Root object assigned");
        } else {
            rootObject.setData("Initial value of Root");
        }
    }
}
