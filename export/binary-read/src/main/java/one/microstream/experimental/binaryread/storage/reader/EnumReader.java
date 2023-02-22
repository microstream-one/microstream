package one.microstream.experimental.binaryread.storage.reader;

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

import one.microstream.experimental.binaryread.structure.EntityMember;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberEnumConstant;

/**
 * This reader does not actually read data from the storage but return information from
 * the TypeDefinitionMember.
 */
public class EnumReader extends MemberReader
{
    protected EnumReader(final EntityMember entityMember)
    {
        super(entityMember);
    }

    @Override
    public long totalLength()
    {
        return 0;  // As it does not read from storage
    }

    @Override
    public <T> T read()
    {
        final PersistenceTypeDefinitionMemberEnumConstant typeDefinitionMember = (PersistenceTypeDefinitionMemberEnumConstant) entityMember.getTypeDefinitionMember();
        return (T) typeDefinitionMember.name();  // String = enum value name
    }
}
