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
import one.microstream.experimental.binaryread.exception.DataStorageDeserializerNotDefinedYetException;
import one.microstream.experimental.binaryread.structure.EntityMemberType;

import java.util.EnumMap;
import java.util.Map;

public final class DataStorageDeserializerFactory
{

    private final ReadingContext readingContext;

    private final Map<EntityMemberType, DataStorageDeserializer> deserializers = new EnumMap<>(EntityMemberType.class);


    public DataStorageDeserializerFactory(final ReadingContext readingContext)
    {
        this.readingContext = readingContext;
    }

    public DataStorageDeserializer getDataStorageDeserializer(final EntityMemberType entityMemberType)
    {
        return deserializers.computeIfAbsent(entityMemberType, this::createDataStorageDeserializer);
    }

    private DataStorageDeserializer createDataStorageDeserializer(final EntityMemberType entityMemberType)
    {
        DataStorageDeserializer result = null;
        switch (entityMemberType)
        {


            case STRING:
                result = new DataStorageStringDeserializer(readingContext.getStorage());
                break;
            case PRIMITIVE:
                result = new DataStoragePrimitiveDeserializer();
                break;
            case PRIMITIVE_WRAPPER:
                result = new DataStoragePrimitiveWrapperDeserializer(readingContext.getStorage());
                break;
            case PRIMITIVE_COLLECTION:
                result = new DataStoragePrimitiveCollectionDeserializer(readingContext);
                break;
            case ENUM:
                result = new DataStorageEnumDeserializer(readingContext.getStorage());
                break;
            case OPTIONAL:
                result = new DataStorageOptionalDeserializer(readingContext);
                break;
            case TIMESTAMP_BASED:
                result = new DataStorageTimeStampDeserializer(readingContext.getStorage());
                break;
            case ENUM_ARRAY:
                result = new DataStorageEnumArrayDeserializer(readingContext.getStorage());
                break;
            case COLLECTION:
            case DICTIONARY:
            case ARRAY:
            case COMPLEX:
            case REFERENCE:
                // All these types, collection, map, complex, etc ..., we just return the reference = ObjectId
                result = new DataStorageReferenceDeserializer();
                break;

            default:
                throw new DataStorageDeserializerNotDefinedYetException(entityMemberType);
        }
        return result;
    }
}
