package one.microstream.integrations.quarkus.deployment.test;

import one.microstream.integrations.quarkus.types.config.StorageManagerInitializer;
import one.microstream.storage.types.StorageManager;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SomeInitializer implements StorageManagerInitializer
{
    @Override
    public void initialize(StorageManager storageManager)
    {
        Object rootObject = storageManager.root();
        if (rootObject == null) {
            SomeRoot root = new SomeRoot();
            root.setData("Initial value");
            storageManager.setRoot(root);
        } else {
            throw new IllegalStateException("StorageManager should not have already a Root object assigned");
        }
    }
}
