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

import one.microstream.experimental.binaryread.exception.NoRootFoundException;
import one.microstream.experimental.binaryread.storage.CachedStorageBytes;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;
import one.microstream.persistence.types.PersistenceTypeDictionary;
import one.microstream.storage.types.StorageDataInventoryFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Storage implements Closeable
{

    private static final Logger LOGGER = LoggerFactory.getLogger(Storage.class);

    // "one.microstream.persistence.types.PersistenceRootReference$Default"
    // holds the reference the Root defined by developer.
    private static final String PERSISTENCE_ROOTS_REFERENCE_DEFAULT = "one.microstream.persistence.types.PersistenceRootReference$Default";

    private final List<StorageDataInventoryFile> files;
    private final PersistenceTypeDictionary typeDictionary;


    private final Map<Long, Entity> entityByObjectId = new HashMap<>();

    private final Map<Long, String> enumValues = new HashMap<>();

    private Entity rootEntity;


    private Storage(final List<StorageDataInventoryFile> files, final PersistenceTypeDictionary typeDictionary)
    {
        this.files = files;
        this.typeDictionary = typeDictionary;
    }

    private Map<Long, List<Entity>> initialRead()
    {
        LOGGER.info("Scanning Data Storage");
        final Map<Long, List<Entity>> entityByTypeId = new HashMap<>();

        CachedStorageBytes cachedStorage = CachedStorageBytes.getInstance();
        for (final StorageDataInventoryFile file : files)
        {

            long pos = 0;
            long fileSize = file.size();  // This is an expensive operation!
            while (pos < fileSize)
            {

                // Read the header of the entity block.
                final Entity entity = cachedStorage.readEntityHeader(file, pos);
                entityByObjectId.put(entity.getObjectId(), entity);

                // Keep a list of
                final List<Entity> entities = entityByTypeId.computeIfAbsent(entity.getTypeId(), x -> new ArrayList<>());
                entities.add(entity);

                saveStructure(entity, typeDictionary.lookupTypeById(entity.getTypeId()));
                pos = pos + entity.getTotalLength();
            }
        }
        return entityByTypeId;
    }

    public void close()
    {
        files.forEach(c -> c.file()
                .useReading()
                .close());
        // FIXME Any data to be removed from the storage?
    }

    private void saveStructure(final Entity entity, final PersistenceTypeDefinition typeDefinition)
    {
        long pos = entity.getPos() + 3 * Long.BYTES; // Start after the header

        // Based on info from Type Dictionary, define the
        for (final PersistenceTypeDefinitionMember typeDefinitionMember : typeDefinition.allMembers())
        {
            final EntityMember entityMember = new EntityMember(entity, typeDefinitionMember, pos);

            entity.addMember(entityMember);

            pos = pos + entityMember.getReader()
                    .totalLength();
        }

    }

    private void defineRoot(final Map<Long, List<Entity>> entityByTypeId)
    {
        LOGGER.info("Defining Root object");

        final PersistenceTypeDefinition persistenceRootsTypeDefinition = typeDictionary.lookupTypeByName(PERSISTENCE_ROOTS_REFERENCE_DEFAULT);
        final List<Entity> entities = entityByTypeId.get(persistenceRootsTypeDefinition.typeId());

        // Take the last one. If multiple, the others are older versions as we only can have 1 active
        final Entity entity = entities.get(entities.size() - 1);

        final Long rootObjectId = entity.getMembers()
                .get(0)
                .getReader()
                .read();

        rootEntity = entityByObjectId.get(rootObjectId);
        if (rootEntity == null) {
            throw new NoRootFoundException(rootObjectId);
        }

        LOGGER.info(String.format("Root object is of type '%s'", typeDictionary.lookupTypeById(rootEntity.getTypeId())
                .runtimeTypeName()));
    }

    private void determineEnumEntities()
    {
        // Each enum value is stored as an entity. We already identify the ObjectIds of such Enum entities.

        entityByObjectId.entrySet()
                .stream()
                .filter(entry -> hasEnumMember(entry.getValue()
                                                       .getMembers()))
                .map(Map.Entry::getKey)
                .forEach(id -> enumValues.put(id, null));
    }

    private boolean hasEnumMember(final List<EntityMember> members)
    {
        return members.stream()
                .anyMatch(em -> em.getEntityMemberType() == EntityMemberType.ENUM);
    }

    /**
     * Returns the enum name or null when not a enum reference.
     *
     * @return enum name or null when not a enum
     */
    public String getEnumValue(final Long reference)
    {
        String result = null;
        if (enumValues.containsKey(reference))
        {
            result = enumValues.computeIfAbsent(reference, this::computeEnumValue);
        }
        return result;
    }

    private String computeEnumValue(final Long ref)
    {
        final Entity entity = entityByObjectId.get(ref);
        final EntityMember entityMember = entity.getMembers()
                .stream()
                .filter(this::isEnumValueMember)
                .findAny()
                .orElseThrow(() -> new RuntimeException("TODO"));
        final Long enumValueRef = entityMember.getReader()
                .read();
        final Entity enumValueEntity = entityByObjectId.get(enumValueRef);
        return enumValueEntity.getEntityMember("value")
                .getReader()
                .read();
    }

    private boolean isEnumValueMember(final EntityMember entityMember)
    {
        final PersistenceTypeDefinitionMember typeDefinitionMember = entityMember.getTypeDefinitionMember();
        return "java.lang.Enum".equals(typeDefinitionMember.qualifier()) &&
                "name".equals(typeDefinitionMember.name());

    }

    public Entity getRoot()
    {
        return rootEntity;
    }


    public Entity getEntityByObjectId(final long objectId)
    {
        return entityByObjectId.get(objectId);
    }


    /**
     * Create a Storage object out of the Data storage. Important, this is a snapshot and might
     * be
     *
     * @param files
     * @param typeDictionary
     * @return
     */
    public static Storage create(final List<StorageDataInventoryFile> files, final PersistenceTypeDictionary typeDictionary)
    {
        // FIXME, We need a copy for a system that is not guaranteed to be 'fixed'.
        // use the https://github.com/microstream-one/microstream/blob/master/storage/embedded-tools/storage-converter/pom.xml
        // to create a copy?
        final Storage storage = new Storage(files, typeDictionary);

        final Map<Long, List<Entity>> entityByTypeId = storage.initialRead();
        storage.defineRoot(entityByTypeId);
        storage.determineEnumEntities();

        return storage;
    }

}
