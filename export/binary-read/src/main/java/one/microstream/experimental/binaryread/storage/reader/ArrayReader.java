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

import one.microstream.experimental.binaryread.exception.UnhandledTypeException;
import one.microstream.experimental.binaryread.structure.EntityMember;
import one.microstream.experimental.binaryread.structure.util.BinaryData;
import one.microstream.experimental.binaryread.structure.util.ValidationUtil;

import java.nio.ByteBuffer;

/**
 * Reader for collections of characters or bytes.
 * TODO Can this be merged with SpecialListReader.
 */
public class ArrayReader extends MemberReader
{

    private long totalLength = -1; // -1 means not yet loaded
    private int arraySize = -1; // -1 means not yet loaded

    protected ArrayReader(final EntityMember entityMember)
    {
        super(entityMember);
    }

    @Override
    public long totalLength()
    {
        if (totalLength < 0)
        {
            loadLengthFromStorage();
        }
        return totalLength;
    }

    private void loadLengthFromStorage()
    {
        final ByteBuffer buff = readData(Long.BYTES * 2);

        totalLength = BinaryData.bytesToLong(buff);
        arraySize = ValidationUtil.longAsSize(BinaryData.bytesToLong(buff, Long.BYTES));

    }

    @Override
    public <T> T read()
    {
        final String typeName = entityMember.getTypeDefinitionMember()
                .typeName();
        if ("[char]".equals(typeName))
        {
            return (T) readString();
        }
        if ("[byte]".equals(typeName))
        {
            return (T) readByteArray();
        }
        throw new UnhandledTypeException("ArrayReader", typeName);
    }

    private byte[] readByteArray()
    {
        final ByteBuffer buff = readData(totalLength);

        final byte[] result = new byte[arraySize];
        int idx = 2 * Long.BYTES;
        for (int i = 0; i < arraySize; i++)
        {
            result[i] = buff.get(idx++);
        }
        return result;
    }

    private String readString()
    {
        final ByteBuffer buff = readData(totalLength);
        return BinaryData.bytesToString(buff, 0);
    }
}
