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

import one.microstream.experimental.binaryread.ReadingContext;
import one.microstream.experimental.binaryread.structure.EntityMember;

import java.nio.ByteBuffer;

/**
 * A reader for reading a reference to another object, a long indicating the ObjectId.
 */
public class ReferenceReader extends MemberReader
{


    protected ReferenceReader(final ReadingContext readingContext, final EntityMember entityMember)
    {
        super(readingContext, entityMember);
    }

    @Override
    public long totalLength()
    {
        // Always fixed length
        return entityMember.getTypeDefinitionMember()
                .persistentMinimumLength();
    }

    @Override
    public <T> T read()
    {
        // We assume this is always a Long (with length 8)
        // A reference should be an ObjectId.
        final ByteBuffer buff = readData(entityMember.getTypeDefinitionMember()
                                           .persistentMinimumLength());
        final Long result = readingContext.getBinaryDataHelper().bytesToLong(buff);
        return (T) result;
    }
}
