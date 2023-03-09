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
import one.microstream.experimental.binaryread.structure.util.ValidationUtil;

import java.nio.ByteBuffer;

/**
 * The abstract reader that reads the 'property' from the storage in binary format. These readers
 * are coarse grained in comparison with the Binary readers from MicroStream core itself as we don't need
 * to recreate the Java instances. Only want to retrieve the data.
 */
public abstract class MemberReader
{
    // FIXME This assumes the storage doesn't change. Because householding process can remove
    // information that we attempt to read!
    protected final EntityMember entityMember;

    protected final ReadingContext readingContext;

    protected MemberReader(final ReadingContext readingContext, final EntityMember entityMember)
    {
        this.readingContext = readingContext;
        this.entityMember = entityMember;
    }

    protected ByteBuffer readData(final long size)
    {
        return readData(ValidationUtil.longAsSize(size));
    }

    protected ByteBuffer readData(final int size)
    {
        final ByteBuffer buff = ByteBuffer.allocate(size);
        // This readBytes is slow due to checking the fileSize every time.
        entityMember.getEntity()
                .getDataFile()
                .readBytes(buff, entityMember.getPos());
        return buff;
    }

    /**
     * Defines the number of bytes the reader will read at maximum from the binary storage. This
     * is used when the initial scan of the storage is performed to identify all objects and their respectively type
     * without actual reading the data.
     * This value is determined based on the PersistenceTypeDefinitionMember, part of the MicroStream Type Dictionary,
     * which is referenced by entityMember.
     *
     * @return number of bytes read at maximum from the binary storage.
     */
    public abstract long totalLength();

    /**
     * Representation of the binary data within the storage.  The type is different based on the reader
     * and the data itself. It can be primitives, like Integers and Doubles, Strings, List, etc...
     *
     * @param <T> Type for auto casting
     * @return Representation of the binary data within the storage.
     */
    public abstract <T> T read();
}
