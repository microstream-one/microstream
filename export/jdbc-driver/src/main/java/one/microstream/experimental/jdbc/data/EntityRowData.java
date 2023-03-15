package one.microstream.experimental.jdbc.data;

/*-
 * #%L
 * jdbc-driver
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

import one.microstream.experimental.binaryread.exception.InvalidObjectIdFoundException;
import one.microstream.experimental.binaryread.storage.ConstantRegistry;
import one.microstream.experimental.binaryread.structure.Entity;
import one.microstream.experimental.binaryread.structure.EntityMember;
import one.microstream.experimental.binaryread.structure.EntityMemberType;
import one.microstream.experimental.binaryread.structure.Storage;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation with the `Entity` as building block.
 */
public class EntityRowData implements RowData
{

    // Duplicated from
    private static final long START_CID_BASE = 9_000_000_000_000_000_000L;

    private final JDBCReadingContext jdbcReadingContext;

    private final Storage storage;

    private final Entity entity;

    public EntityRowData(final JDBCReadingContext jdbcReadingContext, final Entity entity)
    {
        this.jdbcReadingContext = jdbcReadingContext;
        this.storage = jdbcReadingContext.getReadingContext()
                .getStorage();
        this.entity = entity;
    }


    public List<RowData> getSubRowData(final String name)
    {
        final List<RowData> result = new ArrayList<>();

        final Optional<EntityMemberType> entityMemberTypeOptional = entity.findEntityMember(name)
                .map(EntityMember::getEntityMemberType);
        if (entityMemberTypeOptional.isPresent())
        {
            final EntityMember entityMember = entity.getEntityMember(name);

            final Long reference = entityMember.getReader()
                    .read();
            if (reference > 0)
            {
                // Non null reference

                final Entity referenceEntity = storage.getEntityByObjectId(reference);

                final EntityMemberType entityMemberType = entityMemberTypeOptional.get();

                if (entityMemberType == EntityMemberType.COLLECTION
                        || entityMemberType == EntityMemberType.COMPLEX)
                {
                    // Collection, so add the items in collection into result
                    final List<Long> itemObjectIds = referenceEntity.getMembers()
                            .get(0)
                            .getReader()
                            .read();

                    result.addAll(itemObjectIds.stream()
                                          .filter(id -> id > 0) // Filter out null items.
                                          .map(this::getRowData)
                                          .collect(Collectors.toList()));

                }

                if (entityMemberType == EntityMemberType.REFERENCE)
                {
                    // reference, so just pass entity itself for next step
                    result.add(new EntityRowData(jdbcReadingContext, referenceEntity));

                }
            }
        }
        return result;
    }

    private RowData getRowData(final Long objectId)
    {
        if (objectId > START_CID_BASE)
        {
            return new ConstantRowData(ConstantRegistry.lookupObject(objectId));
        }
        else
        {
            return new EntityRowData(jdbcReadingContext, storage.getEntityByObjectId(objectId));
        }
    }

    public Object getValue(final String name)
    {
        final Optional<EntityMember> entityMember = entity.findEntityMember(name);
        return entityMember.map(this::getValue)
                .orElse(null);  // return null for unknown name.
    }

    private Object getValue(final EntityMember entityMember)
    {
        // FIXME extract common code from DataExporter
        Object result = null;
        switch (entityMember.getEntityMemberType())
        {

            case STRING:
                final Long stringRef = entityMember.getReader()
                        .read();

                final Entity stringEntity = storage.getEntityByObjectId(stringRef);
                if (stringEntity == null)
                {
                    // null String value
                    result = null;
                }
                else
                {
                    final Object value = stringEntity.getEntityMember("value")
                            .getReader()
                            .read();

                    result = value.toString();  // TODO Can this be null?

                }
                break;
            case PRIMITIVE:
                result = entityMember.getReader()
                        .read();
                break;
            case PRIMITIVE_WRAPPER:
                final Long reference = entityMember.getReader()
                        .read();
                result = handlePrimitiveWrapper(storage, reference, entityMember.getTypeDefinitionMember()
                        .typeName());
                break;
            case PRIMITIVE_COLLECTION:
                final Long collectionReference = entityMember.getReader()
                        .read();
                final Entity collectionEntity = storage.getEntityByObjectId(collectionReference);
                if (collectionEntity != null)
                {
                    // FIXME Check if there is only 1 member?
                    result = collectionEntity.getMembers()
                            .get(0)
                            .getReader()
                            .read();
                } // else null array and null result is ok
                break;
            case ENUM:
                final Long enumObjectId = entityMember.getReader()
                        .read();
                result = storage.getEnumValue(enumObjectId);
                break;
            // FIXME Are these empty case blocks not needed at all?
            case REFERENCE:
            case COLLECTION:
            case DICTIONARY:
            case OPTIONAL:
            case TIMESTAMP_BASED:
            case ARRAY:
            case ENUM_ARRAY:
            case COMPLEX:
                break;

        }


        return result;
    }

    private Object handlePrimitiveWrapper(final Storage storage, final Long reference, final String type)
    {
        if (reference > START_CID_BASE)
        {
            // A constant
            final Object constantObject = ConstantRegistry.lookupObject(reference);
            if (constantObject == null)
            {
                throw new InvalidObjectIdFoundException("cached instance", reference);
            }
            return constantObject;
        }
        else
        {

            final Entity valueEntity = storage.getEntityByObjectId(reference);
            if (valueEntity == null || valueEntity.getMembers()
                    .isEmpty())
            {
                return null;
            }
            else
            {
                final String enumValue = storage.getEnumValue(valueEntity.getObjectId());
                if (enumValue != null)
                {
                    return enumValue;
                }
                final EntityMember entityMember = valueEntity.getEntityMember("value");
                final Object value = entityMember.getReader()
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
                return performConversion(value, actualType);
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
            return value;
        }
        // TODO How can we find all other cases??

        return value;
    }
}
