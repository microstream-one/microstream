package one.microstream.integrations.quarkus.deployment;

import io.quarkus.test.QuarkusUnitTest;
import one.microstream.integrations.quarkus.deployment.test.SomeInitializerForStorage;
import one.microstream.integrations.quarkus.deployment.test.SomeRootWithStorage;
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


public class ExtensionRootBeanTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(() ->
                    ShrinkWrap.create(JavaArchive.class)
                            .addClasses(SomeInitializerForStorage.class, SomeRootWithStorage.class, CleanupUtil.class)
                            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
            );

    @AfterEach
    public void cleanup() {
        CleanupUtil.deleteDirectory(new File("storage"));
    }

    @Inject
    StorageManager storageManager;

    // Required to trigger creation of Root and execution of RootCreator.
    @Inject
    SomeRootWithStorage root;

    @Test
    public void testRootBeanCreation() {
        Assertions.assertNotNull(storageManager);
        Assertions.assertTrue(storageManager.isRunning());
        Assertions.assertNotNull(storageManager.root());
        Assertions.assertInstanceOf(SomeRootWithStorage.class, storageManager.root());
        SomeRootWithStorage root = (SomeRootWithStorage) storageManager.root();
        Assertions.assertEquals("Initial value of Root", root.getData());
    }
}
