package one.microstream.integrations.quarkus.deployment.test;

import one.microstream.integrations.quarkus.types.config.EmbeddedStorageFoundationCustomizer;
import one.microstream.storage.embedded.types.EmbeddedStorageFoundation;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SomeCustomizer implements EmbeddedStorageFoundationCustomizer
{
    @Override
    public void customize(EmbeddedStorageFoundation<?> embeddedStorageFoundation)
    {
        embeddedStorageFoundation.setDataBaseName("JUnit");
    }
}
