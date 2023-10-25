package test.microstream.integrations.spring.boot;

public class TestData
{
    private String value = "";

    public TestData()
    {
    }

    public TestData(String value)
    {
        this.value = value;
    }

    public String getValue()
    {
        return value;
    }
}
