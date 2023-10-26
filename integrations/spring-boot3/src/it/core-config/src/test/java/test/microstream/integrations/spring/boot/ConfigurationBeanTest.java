package test.microstream.integrations.spring.boot;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import one.microstream.integrations.spring.boot.types.config.StorageManagerConfiguration;
import one.microstream.integrations.spring.boot.types.config.StorageManagerProvider;
import one.microstream.storage.embedded.types.EmbeddedStorageFoundation;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;

@SpringBootTest
public class ConfigurationBeanTest
{
    @Autowired
    StorageManagerConfiguration configuration;

    @Autowired
    StorageManagerProvider provider;

    private final String TEST_SENTENCE = "I love MicroStream Spring Extension";

    @Test
    void testReloadData()
    {
        Assertions.assertNotNull(configuration);
        Assertions.assertNotNull(provider);

        EmbeddedStorageFoundation<?> embeddedStorageFoundation = provider.embeddedStorageFoundation(configuration.getValues());
        TestData data = new TestData(TEST_SENTENCE);
        try (EmbeddedStorageManager storage = embeddedStorageFoundation.start(data)) {
        }

        EmbeddedStorageFoundation<?> embeddedStorageFoundation2 = provider.embeddedStorageFoundation(configuration.getValues());
        TestData data2 = new TestData();
        try (EmbeddedStorageManager storage2 = embeddedStorageFoundation2.start(data2)) {
            Assertions.assertEquals(TEST_SENTENCE, data2.getValue());
        }
    }

    @Test
    void testReloadDataWithoutFoundation()
    {
        Assertions.assertNotNull(configuration);
        Assertions.assertNotNull(provider);

        try (EmbeddedStorageManager storageManager = provider.create(configuration.getValues())) {
            TestData data = new TestData(TEST_SENTENCE);
            storageManager.start();
            storageManager.setRoot(data);
            storageManager.storeRoot();
        }

        try (EmbeddedStorageManager storageManager2 = provider.create(configuration.getValues())) {

            TestData data2 = new TestData();
            storageManager2.start();
            TestData readData = (TestData) storageManager2.root();
            Assertions.assertEquals(TEST_SENTENCE, readData.getValue());

        }
    }

}
