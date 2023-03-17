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

import one.microstream.experimental.binaryread.data.ConvertedData;
import one.microstream.experimental.binaryread.storage.ConstantRegistry;
import one.microstream.experimental.binaryread.structure.Entity;
import one.microstream.experimental.binaryread.structure.EntityMember;
import one.microstream.experimental.binaryread.structure.EntityMemberType;
import one.microstream.experimental.binaryread.structure.Storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static one.microstream.experimental.binaryread.data.DataStorageDeserializer.START_CID_BASE;

/**
 * Implementation with the `Entity` as building block.
 */
public class EntityRowData implements RowData
{

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
        final ConvertedData convertedData =  jdbcReadingContext.getReadingContext()
                .getDataStorageDeserializerFactory()
                .getDataStorageDeserializer(entityMember.getEntityMemberType())
                .resolve(entityMember);
        if (!convertedData.isResolved()) {
            // We take the reference TODO Review Is this enough to have ObjectId on th client?
            return convertedData.getReference();
        }
        return convertedData.getData();
    }
}
