package one.microstream.experimental.binaryread.data;

/*-
 * #%L
 * binary-read
 * %%
 * Copyright (C) 2019 - 2023 MicroStream Software
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
import one.microstream.experimental.binaryread.storage.reader.helper.KeyValueEntry;
import one.microstream.experimental.binaryread.structure.Entity;
import one.microstream.experimental.binaryread.structure.EntityMember;
import one.microstream.experimental.binaryread.structure.EntityMemberType;
import one.microstream.experimental.binaryread.structure.Storage;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberFieldGenericComplex;
import one.microstream.persistence.types.PersistenceTypeDescriptionMemberFieldGeneric;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DataStoragePrimitiveCollectionDeserializer implements DataStorageDeserializer
{

    private final Storage storage;
    private final ReadingContext readingContext;

    public DataStoragePrimitiveCollectionDeserializer(final ReadingContext readingContext)
    {
        this.readingContext = readingContext;
        this.storage = readingContext.getStorage();
    }

    public ConvertedData resolve(final EntityMember entityMember)
    {
        final Long collectionReference = entityMember.getReader()
                .read();
        return resolve(collectionReference, DeserializerOptions.EMPTY);
    }

    public ConvertedData resolve(final Long reference, final DeserializerOptions options)
    {
        Object result = null;
        final Entity collectionEntity = storage.getEntityByObjectId(reference);
        if (collectionEntity != null)
        {
            // FIXME Check if there is only 1 member?
            List<Object> collection = collectionEntity.getMembers()
                    .get(0)
                    .getReader()
                    .read();

            if (collection != null && !collection.isEmpty())
            {

                // The above collection can be our result, or a collection of references, depending on the type we are exporting.
                if (collectionEntity.getMembers()
                        .get(0)
                        .getTypeDefinitionMember()
                        .hasReferences())
                {

                    if (Map.Entry.class.isAssignableFrom(collection.get(0)
                                                                 .getClass()))
                    {
                        collection = collection.stream()
                                .map(e -> resolveToPrimitives((Map.Entry<Long, Long>) e, options))
                                .collect(Collectors.toList());
                    }
                    else
                    {
                        final PersistenceTypeDefinitionMemberFieldGenericComplex itemTypeDefinition = (PersistenceTypeDefinitionMemberFieldGenericComplex) collectionEntity.getMembers()
                                .get(0)
                                .getTypeDefinitionMember();
                        final PersistenceTypeDescriptionMemberFieldGeneric memberFieldGeneric = itemTypeDefinition.members()
                                .get();

                        // It is a list of references.
                        // Replace the collection with a collection of its real values (resolve references)
                        final DataStorageDeserializer storageDeserializer = readingContext
                                .getDataStorageDeserializerFactory()
                                .getDataStorageDeserializer(EntityMemberType.PRIMITIVE_WRAPPER);
                        final DeserializerOptions deserializerOptions = new DeserializerOptionsBuilder(options)
                                .withTypeName(memberFieldGeneric.typeName())
                                .build();
                        collection = collection.stream()
                                .map(item -> storageDeserializer.resolve((Long) item, deserializerOptions).getData())  // FIXME What if it isn't resolved
                                .collect(Collectors.toList());
                    }
                }
                result = collection;
            }
        } // else null array and null result is ok
        return new ConvertedData(result);
    }

    private Map.Entry<String, String> resolveToPrimitives(final Map.Entry<Long, Long> entry, final DeserializerOptions currentDeserializerOptions)
    {
        final DataStorageDeserializer storageDeserializer = readingContext
                .getDataStorageDeserializerFactory()
                .getDataStorageDeserializer(EntityMemberType.PRIMITIVE_WRAPPER);

        // FIXME, What if the Key or Value is a BigInteger? See performConversion
        final DeserializerOptions deserializerOptions = new DeserializerOptionsBuilder(currentDeserializerOptions)
                .withTypeName("java.lang.String")
                .build();
        final ConvertedData keyObject = storageDeserializer.resolve(entry.getKey(), deserializerOptions);
        final String keyValue = getDefaultedValue(keyObject);
        final ConvertedData valueObject = storageDeserializer.resolve(entry.getValue(), deserializerOptions);
        final String value = getDefaultedValue(valueObject);

        return new KeyValueEntry<>(keyValue, value, currentDeserializerOptions.getEntryMarker());
    }

    private String getDefaultedValue(final ConvertedData convertedData) {
        final Object result = convertedData.getData();
        // FIXME Review, Is it ok to convert this to a String, always? We also have different types here!!
        return result != null ? result.toString() : "";
    }
}
