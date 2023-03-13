package one.microstream.experimental.export;

/*-
 * #%L
 * export
 * %%
 * Copyright (C) 2023 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import one.microstream.experimental.binaryread.exception.InvalidObjectIdFoundException;
import one.microstream.experimental.binaryread.exception.UnexpectedException;
import one.microstream.experimental.binaryread.storage.ConstantRegistry;
import one.microstream.experimental.binaryread.storage.reader.helper.KeyValueEntry;
import one.microstream.experimental.binaryread.structure.Entity;
import one.microstream.experimental.binaryread.structure.EntityMember;
import one.microstream.experimental.binaryread.structure.EntityMemberType;
import one.microstream.experimental.binaryread.structure.Storage;
import one.microstream.experimental.export.config.CSVExportConfiguration;
import one.microstream.experimental.export.writing.CSVWriterHeaders;
import one.microstream.experimental.export.writing.LimitedFileWriters;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberFieldGenericComplex;
import one.microstream.persistence.types.PersistenceTypeDescriptionMemberFieldGeneric;
import one.microstream.persistence.types.PersistenceTypeDictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class DataExporter
{

    // Duplicated from
    private static final long START_CID_BASE = 9_000_000_000_000_000_000L;

    private static final Logger LOGGER = LoggerFactory.getLogger(DataExporter.class);

    private final ArrayDeque<Entity> exportQueue = new ArrayDeque<>();
    private final Storage storage;
    private final PersistenceTypeDictionary typeDictionary;
    private final File exportLocation;
    private final CSVExportConfiguration csvExportConfiguration;

    private final Set<Long> processedObjectIds = new HashSet<>();

    private final LimitedFileWriters limitedFileWriters;

    public DataExporter(final Storage storage, final PersistenceTypeDictionary typeDictionary, final CSVExportConfiguration csvExportConfiguration)
    {
        this.storage = storage;
        this.typeDictionary = typeDictionary;
        this.csvExportConfiguration = csvExportConfiguration;
        this.exportLocation = new File(csvExportConfiguration.getTargetDirectory());
        this.limitedFileWriters = new LimitedFileWriters(csvExportConfiguration.getFileWriterCacheSize());
    }

    public void export()
    {
        LOGGER.info("Starting export");
        queueForExport(storage.getRoot());

        while (!exportQueue.isEmpty())
        {
            exportObject(exportQueue.pop());
        }
        limitedFileWriters.close();
        LOGGER.info("Finished export");
    }

    private void queueForExport(final Entity entity)
    {
        if (!processedObjectIds.contains(entity.getObjectId()))
        {

            exportQueue.offer(entity);
            processedObjectIds.add(entity.getObjectId());
        }
    }

    private void exportObject(final Entity entity)
    {
        final PersistenceTypeDefinition typeDefinition = typeDictionary.lookupTypeById(entity.getTypeId());
        final File csvFile = new File(exportLocation, typeDefinition.runtimeTypeName() + ".csv");
        CSVWriterHeaders writeHeaders = new CSVWriterHeaders(csvExportConfiguration, typeDefinition);
        final Writer writer = limitedFileWriters.get(csvFile.getAbsolutePath(), writeHeaders);
        exportObjectData(writer, entity, typeDefinition);
        /*
        try
        {
            writer.close();
        } catch (final IOException e)
        {
            throw new UnexpectedException("Exception when closing the file", e);
        }

         */
    }

    private void exportObjectData(final Writer writer, final Entity entity, final PersistenceTypeDefinition typeDefinition)
    {
        final StringBuilder data = new StringBuilder();
        data.append(entity.getObjectId());

        for (final PersistenceTypeDefinitionMember member : typeDefinition.allMembers())
        {
            data.append(csvExportConfiguration.getValueDelimiter());
            final EntityMember entityMember = entity.getEntityMember(member.name());
            final Long reference;
            switch (entityMember.getEntityMemberType())
            {

                case REFERENCE:
                    reference = entityMember.getReader()
                            .read();

                    handleReference(data, reference);
                    break;
                case STRING:
                    final Long stringRef = entityMember.getReader()
                            .read();

                    final Entity stringEntity = storage.getEntityByObjectId(stringRef);
                    if (stringEntity == null)
                    {
                        // null String value
                        data.append(" ");
                    }
                    else
                    {
                        final Object value = stringEntity.getEntityMember("value")
                                .getReader()
                                .read();
                        data.append(csvExportConfiguration.getStringsQuote());
                        data.append(value == null ? "" : value.toString());  // TODO Can this be null?
                        data.append(csvExportConfiguration.getStringsQuote());
                    }
                    break;
                case PRIMITIVE:
                    handlePrimitive(data, entityMember);
                    break;
                case PRIMITIVE_WRAPPER:
                    reference = entityMember.getReader()
                            .read();
                    data.append(handlePrimitiveWrapper(reference, member.typeName()));
                    break;
                case PRIMITIVE_COLLECTION:
                    reference = entityMember.getReader()
                            .read();
                    handlePrimitiveCollection(data, reference, false);
                    break;
                case COLLECTION:
                case DICTIONARY:
                    reference = entityMember.getReader()
                            .read();
                    final CollectionPrimitiveItem collectionPrimitiveItem = hasPrimitivesOnly(reference);
                    boolean dictionary = entityMember.getEntityMemberType() == EntityMemberType.DICTIONARY;
                    switch (collectionPrimitiveItem)
                    {

                        case YES:
                            handlePrimitiveCollection(data, reference, dictionary);
                            break;
                        case NO:
                            handleReference(data, reference);
                            break;
                        case EMPTY:
                            // empty or only null items
                            emptyCollection(data, dictionary);
                            break;
                        case NULL:
                            // null collection
                            data.append(" ");
                            break;
                        default:
                            throw new UnsupportedOperationException("Unknown type " + collectionPrimitiveItem);
                    }

                    break;
                case OPTIONAL:
                    // This is the pointer to the Optional
                    reference = entityMember.getReader()
                            .read();
                    handleOptional(data, member, reference);
                    break;
                case TIMESTAMP_BASED:
                    reference = entityMember.getReader()
                            .read();
                    final Entity valueEntity = storage.getEntityByObjectId(reference);
                    if (valueEntity == null)
                    {
                        // null  value
                        data.append(" ");
                    }
                    else
                    {
                        final String names = valueEntity.getMembers()
                                .stream()
                                .map(em -> em.getName())
                                .collect(Collectors.joining(","));
                        Object value = null;
                        if ("timestamp".equals(names))
                        {
                            // Java util Date, sql TimeStamp
                            value = valueEntity.getEntityMember("timestamp")
                                    .getReader()
                                    .read();
                        }
                        if ("year,month,day".equals(names))
                        {
                            // LocalDate -> year:month:day
                            value = valueEntity.getMembers()
                                    .stream()
                                    .map(em -> em.getReader()
                                            .read()
                                            .toString())
                                    .collect(Collectors.joining(":"));
                        }
                        if ("date,time".equals(names))
                        {
                            // LocalDateTime ->
                            final Long dateReference = valueEntity.getEntityMember("date")
                                    .getReader()
                                    .read();
                            final Long timeReference = valueEntity.getEntityMember("time")
                                    .getReader()
                                    .read();
                            final Entity dateEntity = storage.getEntityByObjectId(dateReference);
                            final Entity timeEntity = storage.getEntityByObjectId(timeReference);

                            value = dateEntity.getMembers()
                                    .stream()
                                    .map(em -> em.getReader()
                                            .read()
                                            .toString())
                                    .collect(Collectors.joining(":"));

                            value = value + " " + timeEntity.getMembers()
                                    .stream()
                                    .map(em -> em.getReader()
                                            .read()
                                            .toString())
                                    .collect(Collectors.joining(":"));
                            // TODO improvement?  Convert to epoch?
                        }
                        if ("seconds,nanos".equals(names))
                        {
                            // Instant ->
                            final Long secondsValue = valueEntity.getEntityMember("seconds")
                                    .getReader()
                                    .read();
                            final Integer nanosValue = valueEntity.getEntityMember("nanos")
                                    .getReader()
                                    .read();
                            // convert to epoch
                            value = secondsValue * 1000 + nanosValue / 1_000_000;
                        }

                        data.append(value == null ? "" : value.toString());
                    }

                    break;
                case ARRAY:
                    // We come here when reading Optional.of(String) and BigInteger within collection.
                    data.append(performConversion(entityMember.getReader()
                                                          .read(), member.typeName()));

                    break;
                case COMPLEX:
                    List<?> list = entityMember.getReader()
                            .read();
                    if (list == null || list.isEmpty())
                    {
                        data.append(csvExportConfiguration.getCollectionMarkerStart());
                        data.append(csvExportConfiguration.getCollectionMarkerEnd());
                    }
                    else
                    {
                        if (list.get(0)
                                .getClass()
                                .equals(Long.class))
                        {
                            // List of references, queue the references for export.
                            list.stream()
                                    .map(x -> (Long) x)
                                    .forEach(item ->
                                                     queueForExport(storage.getEntityByObjectId(item)));
                        }
                        if (Map.Entry.class.isAssignableFrom(list.get(0)
                                                                     .getClass()))
                        {
                            // HashMap, queue the key and value references for export but replace primitive references
                            // If HashMap contained key/values that could be inlined (both values), it was already handled
                            list = list.stream()
                                    .map(item ->
                                         {
                                             Map.Entry<Long, Long> entry = (Map.Entry<Long, Long>) item;
                                             Object key;
                                             if (checkIfPrimitiveReference(entry.getKey()))
                                             {
                                                 // FIXME What about BigInteger for example? String is not suited there
                                                 key = handlePrimitiveWrapper(entry.getKey(), "java.lang.String");
                                             }
                                             else
                                             {
                                                 queueForExport(storage.getEntityByObjectId(entry.getKey())); // Queue
                                                 key = entry.getKey(); //keep reference in output.
                                             }
                                             Object value;
                                             if (checkIfPrimitiveReference(entry.getValue()))
                                             {
                                                 // FIXME What about BigInteger for example? String is not suited there
                                                 value = handlePrimitiveWrapper(entry.getValue(), "java.lang.String");
                                             }
                                             else
                                             {
                                                 queueForExport(storage.getEntityByObjectId(entry.getValue())); // Queue
                                                 value = entry.getValue(); //keep reference in output.
                                             }
                                             return new KeyValueEntry<>(key, value, csvExportConfiguration.getDictionaryEntryMarker());
                                         })
                                    .collect(Collectors.toList());
                        }
                        // FIXME Its it possible the some other type as List<Long> and Map.Entry? needs special treatment?
                        // Is it worth to generify this?
                        final String value = list.stream()
                                .map(Object::toString)
                                .collect(Collectors.joining(",", csvExportConfiguration.getCollectionMarkerStart(), csvExportConfiguration.getCollectionMarkerEnd()));
                        data.append(value);
                    }
                    break;
                case ENUM:
                    reference = entityMember.getReader()
                            .read();

                    final String enumValue = storage.getEnumValue(reference);
                    data.append(enumValue == null ? " " : enumValue);
                    break;
                case ENUM_ARRAY:
                    long enumArrayRef = entityMember.getReader()
                            .read();
                    Entity enumArrayEntity = storage.getEntityByObjectId(enumArrayRef);
                    if (enumArrayEntity == null)
                    {
                        data.append(" "); // null array
                    }
                    else
                    {
                        List<Long> enumRefs = enumArrayEntity.getMembers()
                                .get(0)
                                .getReader()
                                .read();
                        data.append(enumRefs.stream()
                                            .map(ref -> storage.getEnumValue(ref))
                                            .collect(Collectors.joining(",", "[", "]")));
                    }
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + entityMember.getEntityMemberType());
            }
        }

        try
        {
            writer.write(data.toString());
            writer.write("\n");

        } catch (final IOException e)
        {
            throw new UnexpectedException("Exception when writing to the file", e);
        }

    }

    private Boolean checkIfPrimitiveReference(final Long reference)
    {
        Boolean result = null;
        if (reference > START_CID_BASE)
        {
            result = true;
        }
        else
        {
            final Entity entityItem = storage.getEntityByObjectId(reference);
            if (entityItem != null)
            {
                // Do we have an existing reference?
                // check type
                if (entityItem.getMembers()
                        .size() == 1)
                {

                    EntityMember entityMember = entityItem.getMembers()
                            .get(0);

                    result = entityMember
                            .getEntityMemberType() == EntityMemberType.PRIMITIVE
                            ||
                            entityMember
                                    .getEntityMemberType() == EntityMemberType.ARRAY; // String
                }
                else
                {
                    // A primitive has never more than 1 member
                    // Except when Enum
                    // Used in case of a List or Set of enums.
                    String enumValue = storage.getEnumValue(entityItem.getObjectId());

                    result = enumValue != null;

                }
            }
        }
        return result;
    }

    private void handleOptional(final StringBuilder data, final PersistenceTypeDefinitionMember member, final Long reference)
    {
        final Entity optionalEntity = storage.getEntityByObjectId(reference);
        // This is the pointer to the value contained in the optional.
        if (optionalEntity == null)
        {
            // null instead op Optional instance
            data.append(" ");
        }
        else
        {
            final Long optionalReference = optionalEntity.getEntityMember("value")
                    .getReader()
                    .read();
            final Object cachedInstance = ConstantRegistry.lookupObject(optionalReference);
            if (cachedInstance != null)
            {
                // We have a cached primitive instance
                data.append(cachedInstance);
            }
            else
            {
                final Entity optionalValueEntity = storage.getEntityByObjectId(optionalReference);

                if (optionalValueEntity == null)
                {
                    // Optional.empty
                    data.append(" ");
                }
                else
                {
                    if (optionalValueEntity.getMembers()
                            .size() == 1)
                    {
                        final EntityMember wrappedEntityMember = optionalValueEntity.getMembers()
                                .get(0);
                        final EntityMemberType entityMemberType = wrappedEntityMember.getEntityMemberType();
                        boolean handled = false;
                        if (entityMemberType == EntityMemberType.PRIMITIVE)
                        {
                            handlePrimitive(data, wrappedEntityMember);
                            handled = true;
                        }
                        if (entityMemberType == EntityMemberType.PRIMITIVE_WRAPPER)
                        {
                            data.append(handlePrimitiveWrapper(wrappedEntityMember.getReader()
                                                                       .read(), member.typeName()));
                            handled = true;
                        }
                        if (entityMemberType == EntityMemberType.ARRAY)
                        {
                            // FIXME Is this only String and BigInteger?
                            // They are handled correctly, other ARRAY types might not
                            data.append(handlePrimitiveWrapper(optionalReference, wrappedEntityMember.getTypeDefinitionMember()
                                    .typeName()));

                            handled = true;
                        }

                        if (!handled)
                        {
                            // At the end, handle it as a reference of not inlined
                            handleReference(data, reference);
                        }
                    }
                    else
                    {
                        String enumValue = storage.getEnumValue(optionalReference);
                        if (enumValue == null)
                        {
                            // A POJO, so we cannot handle the Optional 'inline'
                            handleReference(data, reference);
                        }
                        else
                        {
                            data.append(enumValue);
                        }
                    }
                }
            }
        }
    }

    private CollectionPrimitiveItem hasPrimitivesOnly(final Long reference)
    {
        final Entity entityCollection = storage.getEntityByObjectId(reference);
        if (entityCollection == null)
        {
            // null for collection
            return CollectionPrimitiveItem.NULL;
        }
        if (entityCollection.getMembers()
                .size() > 1)
        {
            // FIXME Is this possible? In which case this can happen?
            throw new RuntimeException("More than 1 member for a Collection");
        }
        // FIXME Check if this is a Complex Type? Or does it not matter
        final List<Object> collectionItems = entityCollection.getMembers()
                .get(0)
                .getReader()
                .read();
        if (csvExportConfiguration.isLenient())
        {
            if (collectionItems.isEmpty())
            {
                // empty
                return CollectionPrimitiveItem.EMPTY;
            }

            // Only check the first one (when not null of course)
            CollectionPrimitiveItem result = null;
            int idx = 0;
            while (result == null && collectionItems.size() > idx)
            {

                Class<?> itemClass = collectionItems.get(idx)
                        .getClass();
                boolean handled = false;
                if (Long.class.isAssignableFrom(itemClass))
                {
                    final Long itemReference = (Long) collectionItems.get(idx);
                    // FIXME Can this be null?
                    if (itemReference != null)
                    {
                        Boolean primitiveReference = checkIfPrimitiveReference(itemReference);
                        if (primitiveReference != null)
                        {
                            if (primitiveReference)
                            {
                                result = CollectionPrimitiveItem.YES;
                            }
                            else
                            {
                                result = CollectionPrimitiveItem.NO;
                            }
                        }
                    }
                    handled = true;
                }
                if (Map.Entry.class.isAssignableFrom(itemClass))
                {
                    final Map.Entry<Long, Long> itemReferences = (Map.Entry<Long, Long>) collectionItems.get(idx);
                    // FIXME checkIfPrimitiveReference can return null, but not in this case!
                    // We need a helper that throws exception or at least something that does not result in NPE.
                    if (checkIfPrimitiveReference(itemReferences.getKey())
                            && checkIfPrimitiveReference(itemReferences.getValue()))
                    {
                        result = CollectionPrimitiveItem.YES;
                    }
                    else
                    {
                        result = CollectionPrimitiveItem.NO;
                    }
                    handled = true;
                }
                if (!handled)
                {
                    // FIXME
                    throw new RuntimeException("A collection of item types " + itemClass.getName() + " is not handled yet");
                }
                idx++;

            }

            // Only null items in collection
            return Objects.requireNonNullElse(result, CollectionPrimitiveItem.EMPTY);

        }
        else
        {
            throw new UnsupportedOperationException("FIXME Implement");
        }
    }

    private String handlePrimitiveWrapper(final Long reference, final String type)
    {
        if (reference > START_CID_BASE)
        {
            // A constant
            final Object constantObject = ConstantRegistry.lookupObject(reference);
            if (constantObject == null)
            {
                throw new InvalidObjectIdFoundException("cached instance", reference);
            }
            return constantObject.toString();
        }
        else
        {

            final Entity valueEntity = storage.getEntityByObjectId(reference);
            if (valueEntity == null || valueEntity.getMembers()
                    .isEmpty())
            {
                // FIXME when valueEntity.getMembers().isEmpty() we should not write out the record.
                // It happened with javax.money.DefaultMonetaryRoundingsSingletonSpi$DefaultCurrencyRounding{}
                // null value
                return " ";
            }
            else
            {
                // Enums List and Sets are handled by this bit.
                final String enumValue = storage.getEnumValue(valueEntity.getObjectId());
                if (enumValue != null)
                {
                    return enumValue;
                }
                final EntityMember entityMember = valueEntity.getEntityMember("value");
                Object value = entityMember.getReader()
                        .read();
                String actualType = type;
                if ("java.lang.Object".equals(type))
                {
                    // If Object, get the actual type for the value entity.
                    actualType = valueEntity.getMembers()
                            .get(0)
                            .getTypeDefinitionMember()
                            .typeName();
                }
                value = performConversion(value, actualType);
                return value == null ? "" : value.toString();
            }
        }
    }

    private Object performConversion(final Object value, final String type)
    {
        if (value == null)
        {
            // Nothing to convert
            return null;
        }
        if ("java.math.BigInteger".equals(type) || "[byte]".equals(type))
        {
            return new BigInteger((byte[]) value).toString();
        }
        if ("java.math.BigDecimal".equals(type))
        {
            // TODO Is this always a good way. Value is String for type BigDecimal but can conversion fail?
            return new BigDecimal((String) value).toString();
        }
        if (String.class.equals(value.getClass()))
        {
            return csvExportConfiguration.getStringsQuote()
                    + value
                    + csvExportConfiguration.getStringsQuote();
        }
        // TODO How can we find all other cases??

        return value;
    }

    private void handlePrimitiveCollection(final StringBuilder data, final Long reference, final boolean dictionary)
    {
        final Entity valueEntity = storage.getEntityByObjectId(reference);
        if (valueEntity != null)
        {
            if (valueEntity.getMembers()
                    .size() > 1)
            {
                throw new RuntimeException("We have more than 1 member for Primitive Collection. Is this possible?");
            }

            List<Object> collection = valueEntity.getMembers()
                    .get(0)
                    .getReader()
                    .read();
            if (collection == null || collection.isEmpty())
            {
                emptyCollection(data, dictionary);
            }
            else
            {
                // The above collection can be our result, or a collection of references, depending on the type we are exporting.
                if (valueEntity.getMembers()
                        .get(0)
                        .getTypeDefinitionMember()
                        .hasReferences())
                {

                    if (Map.Entry.class.isAssignableFrom(collection.get(0)
                                                                 .getClass()))
                    {
                        collection = collection.stream()
                                .map(e -> resolveToPrimitives((Map.Entry<Long, Long>) e))
                                .collect(Collectors.toList());
                    }
                    else
                    {
                        final PersistenceTypeDefinitionMemberFieldGenericComplex itemTypeDefinition = (PersistenceTypeDefinitionMemberFieldGenericComplex) valueEntity.getMembers()
                                .get(0)
                                .getTypeDefinitionMember();
                        final PersistenceTypeDescriptionMemberFieldGeneric memberFieldGeneric = itemTypeDefinition.members()
                                .get();

                        // It is a list of references.
                        // Replace the collection with a collection of its real values (resolve references)
                        collection = collection.stream()
                                .map(item -> handlePrimitiveWrapper((Long) item, memberFieldGeneric.typeName()))
                                .collect(Collectors.toList());
                    }
                }
                String markerStart = dictionary ? csvExportConfiguration.getDictionaryMarkersStart() : csvExportConfiguration.getCollectionMarkerStart();
                String markerEnd = dictionary ? csvExportConfiguration.getDictionaryMarkersEnd() : csvExportConfiguration.getCollectionMarkerEnd();
                data.append(collection.stream()
                                    .map(Object::toString)
                                    .collect(Collectors.joining(",", markerStart, markerEnd)));
            }
        }
        else
        {
            data.append(" ");
        }

    }

    private void emptyCollection(final StringBuilder data, final boolean dictionary)
    {
        if (!dictionary)
        {
            data.append(csvExportConfiguration.getCollectionMarkerStart());
            data.append(csvExportConfiguration.getCollectionMarkerEnd());
        }
        else
        {
            data.append(csvExportConfiguration.getDictionaryMarkersStart());
            data.append(csvExportConfiguration.getDictionaryMarkersEnd());
        }
    }

    private Map.Entry<String, String> resolveToPrimitives(final Map.Entry<Long, Long> entry)
    {
        // FIXME, What if the Key or Value is a BigInteger? See performConversion
        final String keyValue = handlePrimitiveWrapper(entry.getKey(), "java.lang.String");
        final String value = handlePrimitiveWrapper(entry.getValue(), "java.lang.String");

        return new KeyValueEntry<>(keyValue, value, csvExportConfiguration.getDictionaryEntryMarker());
    }

    private void handlePrimitive(final StringBuilder data, final EntityMember entityMember)
    {
        data.append(entityMember.getReader()
                            .read()
                            .toString());
    }

    private void handleReference(final StringBuilder data, final Long reference)
    {
        final String enumValue = storage.getEnumValue(reference);
        if (enumValue == null)
        {
            final Entity entityByObjectId = storage.getEntityByObjectId(reference);
            if (entityByObjectId == null)
            {
                // A null object
                data.append(" ");
            }
            else
            {
                data.append(reference);
                queueForExport(entityByObjectId);
            }
        }
        else
        {
            data.append(enumValue);
        }
    }


    private enum CollectionPrimitiveItem
    {
        YES, NO, EMPTY, NULL
    }
}
