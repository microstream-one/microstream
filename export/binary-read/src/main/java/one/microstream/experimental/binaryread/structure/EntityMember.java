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

import one.microstream.experimental.binaryread.storage.reader.MemberReader;
import one.microstream.experimental.binaryread.storage.reader.MemberReaderFactory;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;

/**
 * A member/property within an entity.
 */
public class EntityMember
{

    private final Entity entity;  // Link back to entity, to have Data file available for example.
    private final PersistenceTypeDefinitionMember typeDefinitionMember;  // Definition of member as in TypeDictionary
    private final long pos; // Where the EntityMember Start [Header]? [Data]  (? means optional depending on type-

    private final EntityMemberType entityMemberType;
    private final MemberReader reader;  // A reader capable of reading the member value.

    public EntityMember(final Entity entity, final PersistenceTypeDefinitionMember typeDefinitionMember, final long pos)
    {
        this.entity = entity;
        this.typeDefinitionMember = typeDefinitionMember;
        this.pos = pos;
        entityMemberType = EntityMemberType.forDefinition(typeDefinitionMember);
        reader = MemberReaderFactory.define(entityMemberType, this);
    }

    public Entity getEntity()
    {
        return entity;
    }

    public long getPos()
    {
        return pos;
    }

    public PersistenceTypeDefinitionMember getTypeDefinitionMember()
    {
        return typeDefinitionMember;
    }

    public EntityMemberType getEntityMemberType()
    {
        return entityMemberType;
    }

    public MemberReader getReader()
    {
        return reader;
    }

    // TODO to be reviewed if needed.
    public String getName()
    {
        return typeDefinitionMember.name();
    }
}
