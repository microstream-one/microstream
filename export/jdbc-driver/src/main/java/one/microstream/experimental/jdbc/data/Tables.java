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

import one.microstream.collections.EqHashEnum;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.experimental.binaryread.storage.ConstantRegistry;
import one.microstream.experimental.binaryread.structure.Entity;
import one.microstream.experimental.binaryread.structure.EntityMember;
import one.microstream.experimental.binaryread.structure.EntityMemberType;
import one.microstream.experimental.binaryread.structure.Storage;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberFieldGenericSimple;
import one.microstream.persistence.types.PersistenceTypeDescriptionMember;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Holds are discovered table names from th storage.
 */
public class Tables
{

    // FIXME Duplicated from
    private static final long START_CID_BASE = 9_000_000_000_000_000_000L;


    public static final String ROOT = "ROOT";
    private final Storage storage;

    private final List<String> tables;

    public Tables(final Storage storage)
    {
        this.storage = storage;
        tables = discoverAllTables();
    }

    public List<String> getTableNames()
    {
        return tables;
    }

    private List<String> discoverAllTables()
    {
        final List<String> result = new ArrayList<>();
        result.add(ROOT);
        result.addAll(discoverTables(ROOT + ".", storage.getRoot()));

        return result;
    }

    private List<String> discoverTables(final String prefix, final Entity entity)
    {
        final List<String> result = new ArrayList<>();
        for (EntityMember member : entity.getMembers())
        {
            // FIXME, There are other types?
            if (member.getEntityMemberType() == EntityMemberType.COLLECTION
                    || member.getEntityMemberType() == EntityMemberType.COMPLEX)
            {
                if (member.getEntityMemberType() == EntityMemberType.COLLECTION)
                {
                    // Only for collection. Complex is just an indication we need to follow the graph
                    // in search for other collections.
                    result.add(prefix + member.getName());
                }
                // The items of a  collection can contain other collections
                discoverSubTables(prefix, result, member);
            }

            // follow the reference
            if (member.getEntityMemberType() == EntityMemberType.REFERENCE)
            {
                discoverSubTables(prefix, result, member);
            }

        }

        return result;
    }

    private void discoverSubTables(final String prefix, final List<String> result, final EntityMember member)
    {
        Entity referenceEntity = null;
        String newPrefix = prefix;
        if (member.getEntityMemberType() == EntityMemberType.COLLECTION
        || member.getEntityMemberType() == EntityMemberType.REFERENCE)
        {
            long reference = member.getReader()
                    .read();
            referenceEntity = storage.getEntityByObjectId(reference);
            newPrefix = prefix + member.getName() + ".";
        }
        if (member.getEntityMemberType() == EntityMemberType.COMPLEX)
        {
            List<Long> referenceList = member.getReader()
                    .read();
            if (!referenceList.isEmpty())
            {
                // We take the first entity from the list, assuming the entire list has the same types.
                // FIXME What about null in the list?
                referenceEntity = storage.getEntityByObjectId(referenceList.get(0));
            }
        }

        if (referenceEntity != null)
        {
            result.addAll(discoverTables(newPrefix, referenceEntity));
        }
    }

    public PersistenceTypeDefinition getTableDefinition(final String tableName)
    {
        if (!tables.contains(tableName))
        {
            return null;
        }
        final List<String> nameParts = new ArrayList<>(Arrays.asList(tableName.split("\\.")));
        PersistenceTypeDefinition currentDefinition = null;
        while (!nameParts.isEmpty())
        {
            String namePart = nameParts.remove(0);
            currentDefinition = getDefinition(currentDefinition, namePart);
        }
        return currentDefinition;
    }

    private PersistenceTypeDefinition getDefinition(final PersistenceTypeDefinition currentDefinition, final String namePart)
    {
        PersistenceTypeDefinition result = null;

        if (ROOT.equalsIgnoreCase(namePart))
        {

            result = storage.getTypeDictionary()
                    .lookupTypeById(storage.getRoot()
                                            .getTypeId());
        }
        else
        {
            PersistenceTypeDefinitionMember member = null;
            for (PersistenceTypeDefinitionMember instanceMember : currentDefinition.instanceMembers())
            {
                if (instanceMember.name()
                        .equals(namePart))
                {
                    member = instanceMember;
                    break;
                }
            }
            // Found the member
            if (member != null)
            {
                // When a List is null,empty or only null items,  we don't have entity to use the entityTypeId.
                // So we have to look for a non-null reference and we do that from the newest to oldest Entities
                // (As that increases the change the have a entity that is still valid)
                final List<Entity> all = storage.getAllEntityByTypeId(currentDefinition.typeId());
                int idx = all.size() - 1;
                while (result == null && idx >= 0)
                {
                    // Start a loop to find a non null item
                    EntityMember entityMember = null;
                    Long objectId = null;
                    while (entityMember == null && idx >= 0)
                    {
                        Entity entity = all.get(idx);
                        EntityMember temp = entity.getEntityMember(member.name());
                        objectId = temp.getReader()
                                .read();
                        if (objectId != 0)
                        {
                            // Non null
                            entityMember = temp;
                        }
                        idx--;

                    }

                    if (entityMember == null)
                    {
                        // We did not find anything. TODO this means ....
                        return null;
                    }

                    if (entityMember.getEntityMemberType() == EntityMemberType.COLLECTION)
                    {
                        final Entity listEntity = storage.getEntityByObjectId(objectId);

                        final String memberOfListEntity = listEntity.getMembers()
                                .get(0)
                                .getName();
                        final EntityMember listMember = listEntity.getEntityMember(memberOfListEntity);
                        final List<Long> listItems = listMember.getReader()
                                .read();
                        // FIXME  What if items in List are not of same type.
                        if (!listItems.isEmpty())
                        {
                            int listIdx = listItems.size() - 1;
                            while (result == null && listIdx >= 0)
                            {
                                Long reference = listItems.get(listIdx);

                                if (reference > 0)
                                {


                                    // constant
                                    if (reference > START_CID_BASE)
                                    {
                                        // A constant
                                        final Object constantObject = ConstantRegistry.lookupObject(reference);
                                        result = createDefinitionForPrimitive(constantObject);
                                    }
                                    else
                                    {
                                        final Entity firstEntityOfList = storage.getEntityByObjectId(listItems.get(listIdx));
                                        result = storage.getTypeDictionary()
                                                .lookupTypeById(firstEntityOfList.getTypeId());
                                    }
                                }
                                listIdx--;
                            }
                        }
                    }

                    if (entityMember.getEntityMemberType() == EntityMemberType.REFERENCE)
                    {
                        final Entity referenceEntity = storage.getEntityByObjectId(objectId);
                        result = storage.getTypeDictionary()
                                .lookupTypeById(referenceEntity.getTypeId());

                    }
                    // FIXME There are more EntityMemberType to consider ?
                }
            }
        }

        return result;
    }

    private PersistenceTypeDefinition createDefinitionForPrimitive(final Object constantObject)
    {

        final PersistenceTypeDefinitionMember member =
                PersistenceTypeDefinitionMemberFieldGenericSimple.New(
                        constantObject.getClass()
                                .getTypeName()
                        , ""
                        , "value"
                        , constantObject.getClass()
                        , false
                        , 1  // FIXME ??
                        , 8 // FIXME ??
                );

        final XGettingEnum<? extends PersistenceTypeDefinitionMember> members = EqHashEnum.New(PersistenceTypeDescriptionMember.identityHashEqualator(), member);

        return PersistenceTypeDefinition.New(-1L, "Special definition", constantObject.getClass()
                .getTypeName(), constantObject.getClass(), members, members);
    }
}
