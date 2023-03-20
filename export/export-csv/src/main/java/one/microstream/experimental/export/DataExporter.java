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

import one.microstream.experimental.binaryread.ReadingContext;
import one.microstream.experimental.binaryread.data.ConvertedData;
import one.microstream.experimental.binaryread.data.DataConversion;
import one.microstream.experimental.binaryread.data.DeserializerOptionsBuilder;
import one.microstream.experimental.binaryread.exception.UnexpectedException;
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
import one.microstream.persistence.types.PersistenceTypeDictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
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

    private final ReadingContext readingContext;
    private final File exportLocation;
    private final CSVExportConfiguration csvExportConfiguration;

    private final Set<Long> processedObjectIds = new HashSet<>();

    private final LimitedFileWriters limitedFileWriters;

    private final List<Long> objectTypesToExport = new ArrayList<>();  // empty means all types are exported.

    private final Set<String> subTrees = new HashSet<>();

    public DataExporter(final ReadingContext readingContext, final CSVExportConfiguration csvExportConfiguration)
    {
        this.readingContext = readingContext;
        this.csvExportConfiguration = csvExportConfiguration;
        this.exportLocation = new File(csvExportConfiguration.getTargetDirectory());
        this.limitedFileWriters = new LimitedFileWriters(csvExportConfiguration.getFileWriterCacheSize());
        processExportFilteringOptions();
    }

    private void processExportFilteringOptions()
    {
        if (!csvExportConfiguration.getFilteringOptions()
                .getClassNames()
                .isEmpty())
        {
            determineTypeIdsToExport();
        }
    }

    private void determineTypeIdsToExport()
    {
        final PersistenceTypeDictionary typeDictionary = readingContext.getStorage()
                .getTypeDictionary();
        objectTypesToExport.addAll(csvExportConfiguration.getFilteringOptions()
                                           .getClassNames()
                                           .stream()
                                           .map(n -> nameToTypeId(typeDictionary, n))
                                           .filter(Objects::nonNull)
                                           .collect(Collectors.toList()));
    }

    private Long nameToTypeId(final PersistenceTypeDictionary typeDictionary, final String name)
    {
        PersistenceTypeDefinition typeDefinition = typeDictionary.lookupTypeByName(name);
        if (typeDefinition == null)
        {
            LOGGER.info(String.format("No TypeId found for '%s' - ignored.", name));
            return null;
        }
        return typeDefinition.typeId();
    }

    public void export()
    {
        LOGGER.info("Starting export");
        queueForExport(readingContext.getStorage()
                               .getRoot());

        while (!exportQueue.isEmpty())
        {
            exportObject(exportQueue.pop());
        }
        limitedFileWriters.close();
        LOGGER.info("Finished export");

        if (csvExportConfiguration.getFilteringOptions()
                .isShowSubTrees())
        {
            LOGGER.info("The following list are the discovered entity names:");
            final ArrayList<String> names = new ArrayList<>(subTrees);
            names.sort(Comparator.naturalOrder());
            names.forEach(System.out::println);
        }

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
        final PersistenceTypeDefinition typeDefinition = readingContext.getStorage()
                .getTypeDictionary()
                .lookupTypeById(entity.getTypeId());
        Writer writer = null;
        if (csvExportConfiguration.getFilteringOptions()
                .isShowSubTrees())
        {
            subTrees.add(entity.getName());
        }
        if (partOfSelectedSubTree(entity) && (objectTypesToExport.isEmpty() || objectTypesToExport.contains(typeDefinition.typeId())))
        {
            final File csvFile = new File(exportLocation, typeDefinition.runtimeTypeName() + ".csv");
            final CSVWriterHeaders writeHeaders = new CSVWriterHeaders(csvExportConfiguration, typeDefinition);
            writer = limitedFileWriters.get(csvFile.getAbsolutePath(), writeHeaders);
        }
        exportObjectData(writer, entity, typeDefinition);
    }

    private boolean partOfSelectedSubTree(final Entity entity)
    {
        boolean result = true;
        final Set<String> subTrees = csvExportConfiguration.getFilteringOptions()
                .getSubTrees();
        if (!subTrees.isEmpty())
        {
            result = subTrees.stream()
                    .anyMatch(n -> entity.getName()
                            .startsWith(n));

        }
        return result;
    }

    private void exportObjectData(final Writer writer, final Entity entity, final PersistenceTypeDefinition typeDefinition)
    {
        // writer is null when we do not need to write to file (because user specified a list of classes to export)
        final StringBuilder data = new StringBuilder();
        data.append(entity.getObjectId());
        Storage storage = readingContext.getStorage();

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

                    handleReference(data, reference, defineEntityName(entity, entityMember));
                    break;
                case STRING:
                    final ConvertedData stringValue = readingContext.getDataStorageDeserializerFactory()
                            .getDataStorageDeserializer(entityMember.getEntityMemberType())
                            .resolve(entityMember);
                    writeString(data, stringValue.getData());
                    break;
                case PRIMITIVE:
                    handlePrimitive(data, entityMember);
                    break;
                case PRIMITIVE_WRAPPER:
                    data.append(handlePrimitiveWrapper(entityMember));
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
                            handleReference(data, reference, defineEntityName(entity, entityMember));
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
                    handleOptional(data, entity, entityMember);
                    break;
                case TIMESTAMP_BASED:
                    final ConvertedData convertedData =
                            readingContext.getDataStorageDeserializerFactory()
                                    .getDataStorageDeserializer(entityMember.getEntityMemberType())
                                    .resolve(entityMember);
                    Object timestampValue = convertedData.getData();
                    data.append(timestampValue == null ? "" : timestampValue);

                    break;
                case ARRAY:
                    DataConversion dataConversion = new DataConversion();
                    data.append(dataConversion.perform(entityMember.getReader()
                                                               .read(), member.typeName()));
                    break;
                case COMPLEX:
                    handleComplex(data, storage, entityMember);
                    break;
                case ENUM:
                    final ConvertedData convertedEnumData = readingContext.getDataStorageDeserializerFactory()
                            .getDataStorageDeserializer(entityMember.getEntityMemberType())
                            .resolve(entityMember);
                    final String enumValue = convertedEnumData.getData();
                    data.append(enumValue == null ? " " : enumValue);
                    break;
                case ENUM_ARRAY:
                    final List<String> enumArray = readingContext.getDataStorageDeserializerFactory()
                            .getDataStorageDeserializer(entityMember.getEntityMemberType())
                            .resolve(entityMember)
                            .getData();

                    if (enumArray == null)
                    {
                        data.append(" "); // null array
                    }
                    else
                    {
                        data.append(enumArray.stream()
                                            .collect(Collectors.joining(",", csvExportConfiguration.getCollectionMarkerStart(), csvExportConfiguration.getCollectionMarkerEnd())));
                    }
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + entityMember.getEntityMemberType());
            }
        }

        if (writer != null)
        {
            try
            {
                writer.write(data.toString());
                writer.write("\n");

            } catch (final IOException e)
            {
                throw new UnexpectedException("Exception when writing to the file", e);
            }
        }

    }

    private String defineEntityName(final Entity entity, final EntityMember entityMember)
    {
        return entity.getName() + '.' + entityMember.getName();
    }

    private void writeString(StringBuilder data, Object stringValue)
    {
        if (stringValue == null)
        {
            data.append(" ");
        }
        else
        {
            data.append(csvExportConfiguration.getStringsQuote());
            data.append(stringValue);
            data.append(csvExportConfiguration.getStringsQuote());

        }
    }

    private void handleComplex(StringBuilder data, Storage storage, EntityMember entityMember)
    {
        Object temp = entityMember.getReader()
                .read();
        if (temp instanceof String)
        {
            // The SpecialListReader return either a List or a String.
            writeString(data, temp);
            return;
        }

        List<?> list = (List<?>) temp;
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
                                 {
                                     final Entity entityByObjectId = storage.getEntityByObjectId(item);
                                     setEntityName(entityByObjectId, entityMember);
                                     queueForExport(entityByObjectId);
                                 });
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
                                     final Entity entityByObjectId = storage.getEntityByObjectId(entry.getKey());
                                     setEntityName(entityByObjectId, entityMember);
                                     queueForExport(entityByObjectId); // Queue
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
                                     Entity entityByObjectId = storage.getEntityByObjectId(entry.getValue());
                                     setEntityName(entityByObjectId, entityMember);
                                     queueForExport(entityByObjectId); // Queue
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
    }

    private void setEntityName(final Entity entity, final EntityMember entityMember)
    {
        if (entity.getName() == null)
        {
            entity.setName(entityMember.getEntity()
                                   .getName() + '.' + entityMember.getName());
        }
    }

    private Boolean checkIfPrimitiveReference(final Long reference)
    {
        final Storage storage = readingContext.getStorage();
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

                    final EntityMember entityMember = entityItem.getMembers()
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
                    final String enumValue = storage.getEnumValue(entityItem.getObjectId());

                    result = enumValue != null;

                }
            }
        }
        return result;
    }

    private void handleOptional(final StringBuilder data, final Entity entity, final EntityMember entityMember)
    {
        final ConvertedData convertedData =
                readingContext.getDataStorageDeserializerFactory()
                        .getDataStorageDeserializer(entityMember.getEntityMemberType())
                        .resolve(entityMember);

        if (!convertedData.isResolved())
        {
            handleReference(data, convertedData.getReference(), defineEntityName(entity, entityMember));
        }
        else
        {
            Object value = convertedData.getData();
            data.append(value == null ? "" : value);
        }
    }

    private CollectionPrimitiveItem hasPrimitivesOnly(final Long reference)
    {
        final Storage storage = readingContext.getStorage();
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

    private String handlePrimitiveWrapper(final EntityMember entityMember)
    {
        final ConvertedData convertedData = readingContext.getDataStorageDeserializerFactory()
                .getDataStorageDeserializer(entityMember.getEntityMemberType())
                .resolve(entityMember);
        final Object value = convertedData.getData();
        return value == null ? "" : value.toString();
    }

    private Object handlePrimitiveWrapper(final Long reference, final String typeName)
    {
        final ConvertedData convertedData = readingContext.getDataStorageDeserializerFactory()
                .getDataStorageDeserializer(EntityMemberType.PRIMITIVE_WRAPPER)
                .resolve(reference, new DeserializerOptionsBuilder().withTypeName(typeName)
                        .build());
        return convertedData.getData();
    }

    private void handlePrimitiveCollection(final StringBuilder data, final Long reference, final boolean dictionary)
    {
        DeserializerOptionsBuilder builder = new DeserializerOptionsBuilder();
        if (dictionary)
        {
            builder.withEntryMarker(csvExportConfiguration.getDictionaryEntryMarker());
        }
        final ConvertedData convertedData = readingContext.getDataStorageDeserializerFactory()
                .getDataStorageDeserializer(EntityMemberType.PRIMITIVE_COLLECTION)
                .resolve(reference, builder.build());
        List<?> collection = convertedData.getData();
        if (collection == null || collection.isEmpty())
        {
            emptyCollection(data, dictionary);
        }
        else
        {
            String markerStart = dictionary ? csvExportConfiguration.getDictionaryMarkersStart() : csvExportConfiguration.getCollectionMarkerStart();
            String markerEnd = dictionary ? csvExportConfiguration.getDictionaryMarkersEnd() : csvExportConfiguration.getCollectionMarkerEnd();
            data.append(collection.stream()
                                .map(Object::toString)
                                .collect(Collectors.joining(",", markerStart, markerEnd)));

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

    private void handlePrimitive(final StringBuilder data, final EntityMember entityMember)
    {
        final ConvertedData value = readingContext.getDataStorageDeserializerFactory()
                .getDataStorageDeserializer(entityMember.getEntityMemberType())
                .resolve(entityMember);

        data.append(value.getData()
                            .toString());
    }

    private void handleReference(final StringBuilder data, final Long reference, final String entityName)
    {
        Storage storage = readingContext.getStorage();
        final Entity entityByObjectId = storage.getEntityByObjectId(reference);
        if (entityByObjectId == null)
        {
            // A null object
            data.append(" ");
        }
        else
        {
            data.append(reference);
            if (entityByObjectId.getName() == null)
            {
                // TODO What about circular ones. We only consider one path and maybe
                // user specified anther path to the entity in the filtering.
                entityByObjectId.setName(entityName);
            }
            queueForExport(entityByObjectId);
        }
    }


    private enum CollectionPrimitiveItem
    {
        YES, NO, EMPTY, NULL
    }
}
