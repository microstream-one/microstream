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
import one.microstream.experimental.binaryread.structure.util.BinaryData;

import java.nio.ByteBuffer;

/**
 * A reader that handles the primitive types.
 */
public class PrimitiveReader extends MemberReader
{

    protected PrimitiveReader(final ReadingContext readingContext, final EntityMember entityMember)
    {
        super(readingContext, entityMember);
    }

    @Override
    public long totalLength()
    {
        return entityMember.getTypeDefinitionMember()
                .persistentMinimumLength();
    }

    @Override
    public <T> T read()
    {
        if ("long".equals(entityMember.getTypeDefinitionMember()
                                  .typeName()))
        {
            return (T) readLong();
        }
        if ("int".equals(entityMember.getTypeDefinitionMember()
                                 .typeName()))
        {
            return (T) readInt();
        }
        if ("boolean".equals(entityMember.getTypeDefinitionMember()
                                     .typeName()))
        {
            return (T) readBoolean();
        }
        if ("double".equals(entityMember.getTypeDefinitionMember()
                                    .typeName()))
        {
            return (T) readDouble();
        }
        if ("float".equals(entityMember.getTypeDefinitionMember()
                                   .typeName()))
        {
            return (T) readFloat();
        }
        if ("char".equals(entityMember.getTypeDefinitionMember()
                                  .typeName()))
        {
            return (T) readChar();
        }
        if ("byte".equals(entityMember.getTypeDefinitionMember()
                                  .typeName()))
        {
            return (T) readByte();
        }
        if ("short".equals(entityMember.getTypeDefinitionMember()
                                   .typeName()))
        {
            return (T) readShort();
        }
        throw new UnsupportedOperationException("Type not handled " + entityMember.getTypeDefinitionMember()
                .typeName());
    }

    private Boolean readBoolean()
    {

        final ByteBuffer buff = readData(entityMember.getTypeDefinitionMember()
                                           .persistentMinimumLength());
        final byte[] bytes = buff.array();
        return bytes[0] != 0;

    }

    private Long readLong()
    {
        final ByteBuffer buff = readData(entityMember.getTypeDefinitionMember()
                                           .persistentMinimumLength());
        return BinaryData.bytesToLong(buff);
    }

    private Integer readInt()
    {
        final ByteBuffer buff = readData(entityMember.getTypeDefinitionMember()
                                           .persistentMinimumLength());
        return BinaryData.bytesToInt(buff);
    }

    private Double readDouble()
    {
        final ByteBuffer buff = readData(entityMember.getTypeDefinitionMember()
                                           .persistentMinimumLength());
        return BinaryData.bytesToDouble(buff);
    }

    private Float readFloat()
    {
        final ByteBuffer buff = readData(entityMember.getTypeDefinitionMember()
                                           .persistentMinimumLength());
        return BinaryData.bytesToFloat(buff);
    }


    private Byte readByte()
    {
        final ByteBuffer buff = readData(entityMember.getTypeDefinitionMember()
                                           .persistentMinimumLength());
        final byte[] bytes = buff.array();
        return bytes[0];
    }

    private Short readShort()
    {
        final ByteBuffer buff = readData(entityMember.getTypeDefinitionMember()
                                           .persistentMinimumLength());
        return BinaryData.bytesToShort(buff);
    }

    private Character readChar()
    {
        final ByteBuffer buff = readData(entityMember.getTypeDefinitionMember()
                                           .persistentMinimumLength());
        return (char) BinaryData.bytesToShort(buff);
    }
}
