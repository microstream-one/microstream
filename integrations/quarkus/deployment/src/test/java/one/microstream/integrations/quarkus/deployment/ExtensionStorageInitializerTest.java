package one.microstream.integrations.quarkus.deployment;

import io.quarkus.test.QuarkusUnitTest;
import one.microstream.integrations.quarkus.deployment.test.SomeInitializer;
import one.microstream.integrations.quarkus.deployment.test.SomeRoot;
import one.microstream.storage.types.StorageManager;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.inject.Inject;
import java.io.File;


public class ExtensionStorageInitializerTest
{

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(() ->
                                        ShrinkWrap.create(JavaArchive.class)
                                                .addClasses(SomeInitializer.class, SomeRoot.class, CleanupUtil.class)
                                                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
            );

    @AfterEach
    public void cleanup()
    {
        CleanupUtil.deleteDirectory(new File("storage"));
    }

    @Inject
    StorageManager storageManager;

    @Test
    public void testInitializer()
    {
        Assertions.assertNotNull(storageManager);
        Assertions.assertTrue(storageManager.isRunning());
        Assertions.assertNotNull(storageManager.root());
        Assertions.assertInstanceOf(SomeRoot.class, storageManager.root());
        SomeRoot root = (SomeRoot) storageManager.root();
        Assertions.assertEquals("Initial value", root.getData());
    }
}
