package one.microstream.integrations.quarkus.deployment.test;

import one.microstream.integrations.quarkus.types.Storage;

@Storage
public class OtherRootWithStorage
{

    private String data;

    public String getData()
    {
        return data;
    }

    public void setData(String data)
    {
        this.data = data;
    }
}
