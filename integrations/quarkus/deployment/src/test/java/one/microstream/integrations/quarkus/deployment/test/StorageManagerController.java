package one.microstream.integrations.quarkus.deployment.test;

import one.microstream.storage.types.StorageManager;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/test")
@Produces(MediaType.TEXT_PLAIN)
public class StorageManagerController
{

    @Inject
    StorageManager storageManager;

    @GET
    public String testStorageManager()
    {
        return "storageManagerRoot=" + (storageManager.root() == null);
    }
}
