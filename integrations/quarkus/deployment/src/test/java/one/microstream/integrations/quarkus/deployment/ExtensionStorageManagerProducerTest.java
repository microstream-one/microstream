package one.microstream.integrations.quarkus.deployment;

import io.quarkus.test.QuarkusUnitTest;
import io.restassured.RestAssured;
import one.microstream.integrations.quarkus.deployment.test.StorageManagerController;
import one.microstream.storage.types.StorageManager;
import org.hamcrest.Matchers;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.inject.Inject;
import java.io.File;

public class ExtensionStorageManagerProducerTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(() ->
                    ShrinkWrap.create(JavaArchive.class)
                            .addClasses(StorageManagerController.class, CleanupUtil.class)
                            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
            );

    @Inject
    StorageManager storageManager;

    @AfterEach
    public void cleanup() {
        CleanupUtil.deleteDirectory(new File("storage"));
    }

    @Test
    public void testInjectionIntoController() {
        RestAssured.when().get("/test").then().body(Matchers.is("storageManagerRoot=true"));
    }

    @Test
    public void testStorageManagerProducer() {
        Assertions.assertNotNull(storageManager);
        Assertions.assertNull(storageManager.root());
        Assertions.assertTrue(storageManager.isRunning());
    }
}
