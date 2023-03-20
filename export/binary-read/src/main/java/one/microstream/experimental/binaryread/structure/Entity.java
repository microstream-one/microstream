package one.microstream.experimental.binaryread.structure;

/*-
 * #%L
 * binary-read
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

import one.microstream.experimental.binaryread.exception.EntityMemberNotFoundException;
import one.microstream.storage.types.StorageDataInventoryFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents an 'entity' within the data storage.
 */
public class Entity
{

    private final long pos;  // Start of the entity [Header] [Data]
    private final StorageDataInventoryFile dataFile;  // File where Entity is located
    private final EntityHeader entityHeader;

    // The members/properties included in this entity.
    private final List<EntityMember> members = new ArrayList<>();

    private String name;  // The name of the entity like ROOT.books

    public Entity(final StorageDataInventoryFile dataFile, final long pos, final EntityHeader entityHeader)
    {
        this.pos = pos;
        this.dataFile = dataFile;
        this.entityHeader = entityHeader;
    }

    /**
     * Location of the entity within the data storage 'file'.
     *
     * @return Location of the entity within the data storage 'file'.
     */
    public long getPos()
    {
        return pos;
    }

    /**
     * The Data Storage File holding this entity.
     *
     * @return The Data Storage File holding this entity.
     */
    public StorageDataInventoryFile getDataFile()
    {
        return dataFile;
    }

    /**
     * Returns the total length of the entity within the data 'file'.
     *
     * @return total length of the entity within the data 'file'.
     */
    public long getTotalLength()
    {
        // For easier access.
        return entityHeader.getTotalLength();
    }

    /**
     * Returns the TypeId of the entity as defined in the Type Dictionary.
     *
     * @return TypeId of the entity as defined in the Type Dictionary.
     */
    public long getTypeId()
    {
        // For easier access.
        return entityHeader.getTypeId();
    }

    /**
     * Returns the ObjectId of the entity.
     *
     * @return ObjectId of the entity.
     */
    public long getObjectId()
    {
        // For easier access.
        return entityHeader.getObjectId();
    }


    /**
     * Adds a EntityMember that describes the structure of the entity. These must be added
     * in the Order as they appear within the binary storage.
     *
     * @param entityMember EntityMember that describes the structure of the entity.
     */
    public void addMember(final EntityMember entityMember)
    {
        members.add(entityMember);
    }

    /**
     * Returns the list of all members describing the structure of the entity.
     *
     * @return all members describing the structure of the entity.
     */
    public List<EntityMember> getMembers()
    {
        return members;
    }

    /**
     * Return the member with the specified name from the list. When no member witch such name
     * exist, it throws a {@code EntityMemberNotFoundException}.
     *
     * @param name name of the member that needs to be found.
     * @return Member with the specified name or exception thrown.
     */
    public EntityMember getEntityMember(final String name)
    {
        return members.stream()
                .filter(em -> em.getName()
                        .equals(name))
                .findAny()
                .orElseThrow(() -> new EntityMemberNotFoundException(name));
    }

    /**
     * Return the member with the specified name from the list. This variation does not throw an exception like
     * {@code getEntityMember()} does.
     *
     * @param name name of the member that needs to be found.
     * @return Member with the specified name.
     */
    public Optional<EntityMember> findEntityMember(final String name)
    {
        return members.stream()
                .filter(em -> em.getName()
                        .equals(name))
                .findAny();
    }

    public String getName()
    {
        return name;
    }

    public void setName(final String name)
    {
        this.name = name;
    }
}
