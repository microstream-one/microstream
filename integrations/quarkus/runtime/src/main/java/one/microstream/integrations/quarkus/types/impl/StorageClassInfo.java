package one.microstream.integrations.quarkus.types.impl;

import java.util.Arrays;
import java.util.List;

/**
 * Information about a class annotated with {@link one.microstream.integrations.quarkus.types.Storage}.
 */
public class StorageClassInfo
{

    private final Class classReference;

    private final List<String> fieldsToInject;

    public StorageClassInfo(final Class classReference, final List<String> fieldsToInject)
    {

        this.classReference = classReference;
        this.fieldsToInject = fieldsToInject;
    }

    public StorageClassInfo(final Class classReference, final String fieldsToInject)
    {

        this.classReference = classReference;
        this.fieldsToInject = Arrays.asList(fieldsToInject.split(","));
    }

    public Class getClassReference()
    {
        return classReference;
    }

    public List<String> getFieldsToInject()
    {
        return this.fieldsToInject;
    }
}
