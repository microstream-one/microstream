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

import one.microstream.collections.types.XGettingSequence;
import one.microstream.experimental.binaryread.ReadingContext;
import one.microstream.experimental.binaryread.storage.CachedStorageBytes;
import one.microstream.experimental.binaryread.storage.reader.helper.KeyValueEntry;
import one.microstream.experimental.binaryread.structure.ArrayHeader;
import one.microstream.experimental.binaryread.structure.EntityMember;
import one.microstream.experimental.binaryread.structure.util.BinaryData;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberFieldGenericComplex;
import one.microstream.persistence.types.PersistenceTypeDescriptionMemberFieldGeneric;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A special type of reader, similar to ArrayReader, that reads collections.
 * TODO Can this be merged with ArrayReader?
 */
public class SpecialListReader extends MemberReader
{

    private long totalLength = -1; // -1 means not yet loaded
    private long arraySize = -1; // -1 means not yet loaded

    protected SpecialListReader(final ReadingContext readingContext, final EntityMember entityMember)
    {
        super(readingContext, entityMember);
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
        final ArrayHeader arrayHeader = CachedStorageBytes.getInstance()
                .readArrayHeader(entityMember.getEntity()
                                         .getDataFile(), entityMember.getPos());

        totalLength = arrayHeader.getTotalLength();
        arraySize = arrayHeader.getSize();
    }

    @Override
    public <T> T read()
    {
        T result = null;
        String typeName = null;
        final PersistenceTypeDefinitionMember typeDefinitionMember = entityMember.getTypeDefinitionMember();
        if (typeDefinitionMember instanceof PersistenceTypeDefinitionMemberFieldGenericComplex)
        {
            final PersistenceTypeDefinitionMemberFieldGenericComplex complex = (PersistenceTypeDefinitionMemberFieldGenericComplex) typeDefinitionMember;
            if (complex.members()
                    .size() > 1)
            {
                typeName = determineComplexType(complex.members());
            }
            else
            {

                for (final PersistenceTypeDescriptionMemberFieldGeneric member : complex.members())
                {
                    typeName = member.typeName();
                }
            }
        }

        if ("[char]".equals(typeName))
        {
            result = (T) asStringList();
        }
        if ("char".equals(typeName))
        {
            result = (T) asString();
        }
        if ("boolean".equals(typeName))
        {
            result = (T) asBooleanList();
        }
        if ("byte".equals(typeName))
        {
            result = (T) asByteList();
        }
        if ("java.lang.Byte".equals(typeName) ||
                "java.lang.Boolean".equals(typeName) ||
                "java.lang.Short".equals(typeName) ||
                "java.lang.Integer".equals(typeName) ||
                "java.lang.Long".equals(typeName) ||
                "java.lang.Double".equals(typeName) ||
                "java.lang.Character".equals(typeName) ||
                "java.lang.Float".equals(typeName))
        {
            // It is a list of references
            result = (T) asLongList();
        }
        if ("short".equals(typeName))
        {
            result = (T) asShortList();
        }
        if ("int".equals(typeName))
        {
            result = (T) asIntList();
        }
        if ("long".equals(typeName))
        {
            result = (T) asLongList();
        }
        if ("float".equals(typeName))
        {
            result = (T) asFloatList();
        }
        if ("double".equals(typeName))
        {
            result = (T) asDoubleList();
        }

        if ("java.lang.Object".equals(typeName))
        {
            // Object means a list of references.
            result = (T) asLongList();
        }
        if ("java.math.BigInteger".equals(typeName) ||
                "java.math.BigDecimal".equals(typeName))
        {

            result = (T) asLongList();
        }

        if ("java.lang.String".equals(typeName))
        {
            result = (T) asLongList();
        }

        if ("map".equals(typeName))
        {
            result = (T) asMapEntryList();
        }

        if (readingContext.getStorage().isEnumClass(typeName)) {
            // We have an array of enum and thus an array of references.
            result = (T) asLongList();
        }

        if (result == null)
        {
            System.out.println("WARNING: Not handled by SpecialListReader : " + typeName);
        }
        return result;
    }

    private String determineComplexType(final XGettingSequence<PersistenceTypeDescriptionMemberFieldGeneric> members)
    {
        String result = null;
        if (members.size() == 2)
        {
            if ("key".equals(members.at(0)
                                     .name()) &&
                    "value".equals(members.at(1)
                                           .name()))
            {
                // Can the type name be something else thn java.lang.Object?
                // TODO is this too greedy and do we need a more fine grained control?
                result = "map";
            }
        }
        if (result == null)
        {
            // FIXME
            throw new RuntimeException("TODO Define the complex type name for " + members);
        }
        return result;
    }

    private List<String> asStringList()
    {
        final ByteBuffer buff = readData(totalLength);

        final List<String> result = new ArrayList<>();

        int idx = 2 * Long.BYTES;
        for (int i = 0; i < arraySize; i++)
        {

            final long bytesLength = BinaryData.bytesToLong(buff, idx);
            result.add(BinaryData.bytesToString(buff, idx));
            idx += bytesLength;
        }

        return result;

    }

    private List<Boolean> asBooleanList()
    {
        final List<Boolean> result = new ArrayList<>();

        final ByteBuffer buff = readData(totalLength);

        final byte[] bytes = buff.array();

        int idx = 2 * Long.BYTES;
        for (int i = 0; i < arraySize; i++)
        {

            final byte b = bytes[idx];
            result.add(b != 0);
            idx += 1;
        }
        return result;
    }

    private List<String> asString()
    {
        final ByteBuffer buff = readData(totalLength);
        final byte[] bytes = buff.array();

        final StringBuilder result = new StringBuilder();
        int idx = 2 * Long.BYTES;
        for (int i = 0; i < arraySize; i++)
        {

            final int ch = bytes[idx] + bytes[idx + 1] * 256;
            result.append((char) ch);
            idx += 2;
        }

        return List.of(result.toString());
    }

    private List<Byte> asByteList()
    {

        final ByteBuffer buff = readData(totalLength);
        final byte[] bytes = buff.array();

        final List<Byte> result = new ArrayList<>();

        int idx = 2 * Long.BYTES;
        for (int i = 0; i < arraySize; i++)
        {
            result.add(bytes[idx]);
            idx += 1;
        }

        return result;

    }

    private List<Short> asShortList()
    {
        final ByteBuffer buff = readData(totalLength);

        final List<Short> result = new ArrayList<>();

        int idx = 2 * Long.BYTES;
        for (int i = 0; i < arraySize; i++)
        {

            result.add(BinaryData.bytesToShort(buff, idx));
            idx += Short.BYTES;
        }

        return result;

    }

    private List<Integer> asIntList()
    {
        final ByteBuffer buff = readData(totalLength);

        final List<Integer> result = new ArrayList<>();

        int idx = 2 * Long.BYTES;
        for (int i = 0; i < arraySize; i++)
        {

            result.add(BinaryData.bytesToInt(buff, idx));
            idx += Integer.BYTES;
        }

        return result;

    }

    private List<Long> asLongList()
    {
        final ByteBuffer buff = readData(totalLength);

        final List<Long> result = new ArrayList<>();

        int idx = 2 * Long.BYTES;
        for (int i = 0; i < arraySize; i++)
        {

            final long value = BinaryData.bytesToLong(buff, idx);
            result.add(value);
            idx += Long.BYTES;
        }

        return result;

    }

    private List<Float> asFloatList()
    {
        final ByteBuffer buff = readData(totalLength);

        final List<Float> result = new ArrayList<>();

        int idx = 2 * Long.BYTES;
        for (int i = 0; i < arraySize; i++)
        {

            result.add(BinaryData.bytesToFloat(buff, idx));
            idx += Float.BYTES;
        }

        return result;

    }

    private List<Double> asDoubleList()
    {
        final ByteBuffer buff = readData(totalLength);

        final List<Double> result = new ArrayList<>();

        int idx = 2 * Long.BYTES;
        for (int i = 0; i < arraySize; i++)
        {

            result.add(BinaryData.bytesToDouble(buff, idx));
            idx += Double.BYTES;
        }

        return result;

    }

    private List<Map.Entry<Long, Long>> asMapEntryList()
    {
        final ByteBuffer buff = readData(totalLength);
        long items = BinaryData.bytesToLong(buff, Long.BYTES);

        final List<Map.Entry<Long, Long>> result = new ArrayList<>();
        int idx = Long.BYTES * 2;
        for (int i = 0; i < items; i++)
        {
            long key = BinaryData.bytesToLong(buff, idx);
            idx += Long.BYTES;
            long value = BinaryData.bytesToLong(buff, idx);
            idx += Long.BYTES;
            result.add(new KeyValueEntry<>(key, value));
        }
        return result;
    }
}
