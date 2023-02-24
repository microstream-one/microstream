package one.microstream.experimental.export.test.model;

import java.util.HashMap;
import java.util.Map;

public class Maps
{

    private Map<String, String> stringMap = new HashMap<>();
    private Map<String, Long> countMap = new HashMap<>();
    private Map<Double, Double> dataMap = new HashMap<>();

    public Map<String, String> getStringMap()
    {
        return stringMap;
    }

    public void setStringMap(final Map<String, String> stringMap)
    {
        this.stringMap = stringMap;
    }

    public Map<String, Long> getCountMap()
    {
        return countMap;
    }

    public void setCountMap(final Map<String, Long> countMap)
    {
        this.countMap = countMap;
    }

    public Map<Double, Double> getDataMap()
    {
        return dataMap;
    }

    public void setDataMap(final Map<Double, Double> dataMap)
    {
        this.dataMap = dataMap;
    }
}
