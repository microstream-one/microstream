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
        if (classReference != Object.class)
        {
            this.classReference = classReference;
        } else {
            // Object.class past since .map entries on BeanCreator blow up when null is specified in value.
            // see https://github.com/quarkusio/quarkus/issues/27664
            this.classReference = null;
        }
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
