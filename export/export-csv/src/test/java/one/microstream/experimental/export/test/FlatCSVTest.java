package one.microstream.experimental.export.test;

/*-
 * #%L
 * export
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

import one.microstream.experimental.export.FlatCSV;
import one.microstream.experimental.export.test.model.BigValues;
import one.microstream.experimental.export.test.model.Dates;
import one.microstream.experimental.export.test.model.Enums;
import one.microstream.experimental.export.test.model.Maps;
import one.microstream.experimental.export.test.model.MyEnum;
import one.microstream.experimental.export.test.model.Optionals;
import one.microstream.experimental.export.test.model.PrimitiveArrays;
import one.microstream.experimental.export.test.model.PrimitiveLists;
import one.microstream.experimental.export.test.model.PrimitiveSets;
import one.microstream.experimental.export.test.model.PrimitiveWrapperArrays;
import one.microstream.experimental.export.test.model.PrimitiveWrappers;
import one.microstream.experimental.export.test.model.SomePrimitives;
import one.microstream.experimental.export.test.model.Strings;
import one.microstream.experimental.export.test.model.TheRoot;
import one.microstream.storage.embedded.types.EmbeddedStorageFoundation;
import one.microstream.storage.types.StorageManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

class FlatCSVTest
{

    @BeforeAll
    public static void cleanup()
    {
        deleteDirectory(new File("target/csv"));
        deleteDirectory(new File("target/data"));
    }

    private static void deleteDirectory(final File directory)
    {
        final String[] entries = directory.list();
        if (entries == null)
        {
            return;
        }
        for (final String file : entries)
        {
            final File currentFile = new File(directory.getPath(), file);
            if (currentFile.isDirectory())
            {
                // delete the directory content and directory itself
                deleteDirectory(currentFile);
            }
            else
            {
                // Just delete the file
                currentFile.delete();
            }
        }
        directory.delete();
    }

    @Test
    void export()
    {
        final TheRoot root = new TheRoot();

        createTestData(root);
        final EmbeddedStorageFoundation<?> foundation = TestConfig.createStorageFoundation();

        final FlatCSV csv = new FlatCSV(foundation, "target/csv");
        csv.export();
    }

    private void createTestData(final TheRoot root)
    {
        try (final StorageManager storageManager = TestConfig.createStorageManager(root))
        {

            root.addSomePrimitives(primitives1());
            root.addSomePrimitives(primitives2());
            root.addPrimitiveWrappers(wrappers1());
            root.addPrimitiveWrappers(wrappers2());
            root.addPrimitiveWrappers(wrappers3());

            root.addPrimitiveArrays(primitivesArrays1());
            root.addPrimitiveArrays(primitivesArrays2());

            root.addPrimitiveWrapperArrays(primitivesWrapperArrays1());
            root.addPrimitiveWrapperArrays(primitivesWrapperArrays2());

            root.addPrimitiveLists(primitivesLists1());
            root.addPrimitiveLists(primitivesLists2());
            root.addPrimitiveLists(primitivesLists3());
            root.addPrimitiveLists(primitivesLists4());

            root.addPrimitiveSets(primitivesSets1());
            root.addPrimitiveSets(primitivesSets2());
            root.addPrimitiveSets(primitivesSets3());
            root.addPrimitiveSets(primitivesSets4());

            root.addOptionals(optionals1());
            root.addOptionals(optionals2());
            root.addOptionals(optionals3());
            root.addOptionals(optionals4());

            root.addStrings(strings1());
            root.addStrings(strings2());
            root.addStrings(strings3());

            root.addEnums(enums1());
            root.addEnums(enums2());
            root.addEnums(enums3());

            root.addBigValues(bigValues1());
            root.addBigValues(bigValues2());
            root.addBigValues(bigValues3());

            root.addDateValues(dates1());
            root.addDateValues(dates2());

            root.addMaps(maps1());
            root.addMaps(maps2());
            root.addMaps(maps3());

            storageManager.store(root.getPrimitives());
            storageManager.store(root.getWrappers());
            storageManager.store(root.getPrimitiveArrays());
            storageManager.store(root.getPrimitiveWrapperArrays());
            storageManager.store(root.getPrimitiveLists());
            storageManager.store(root.getPrimitiveSets());
            storageManager.store(root.getOptionalList());
            storageManager.store(root.getStrings());
            storageManager.store(root.getEnumList());
            storageManager.store(root.getBigValues());
            storageManager.store(root.getDateValues());
            storageManager.store(root.getMaps());
        }


    }

    private SomePrimitives primitives1()
    {
        final SomePrimitives result = new SomePrimitives();
        result.setaBool(true);
        result.setaByte((byte) 123);
        result.setaShort((short) 321);
        result.setAnInt(12345);
        result.setaLong(987654321L);
        result.setaFloat(123.45F);
        result.setaDouble(54321.89);
        result.setaChar('M');
        return result;
    }

    private SomePrimitives primitives2()
    {
        final SomePrimitives result = new SomePrimitives();
        result.setaBool(false);
        result.setaByte((byte) -123);
        result.setaShort((short) -321);
        result.setAnInt(-12345);
        result.setaLong(-987654321L);
        result.setaFloat(-123.45F);
        result.setaDouble(-54321.89);
        result.setaChar('ム');
        return result;
    }

    private PrimitiveWrappers wrappers1()
    {
        final PrimitiveWrappers result = new PrimitiveWrappers();
        result.setaBool(true);
        result.setaByte((byte) 123);
        result.setaShort((short) 321);
        result.setAnInt(12345);
        result.setaLong(987654321L);
        result.setaFloat(123.45F);
        result.setaDouble(54321.89);
        result.setaCharacter('M');
        return result;
    }

    private PrimitiveWrappers wrappers2()
    {
        final PrimitiveWrappers result = new PrimitiveWrappers();
        result.setaBool(false);
        result.setaByte((byte) -123);
        result.setaShort((short) -321);
        result.setAnInt(-12345);
        result.setaLong(-987654321L);
        result.setaFloat(-123.45F);
        result.setaDouble(-54321.89);
        result.setaCharacter('ム');
        return result;
    }

    private PrimitiveWrappers wrappers3()
    {
        final PrimitiveWrappers result = new PrimitiveWrappers();
        result.setaBool(null);
        result.setaByte(null);
        result.setaShort(null);
        result.setAnInt(null);
        result.setaLong(null);
        result.setaFloat(null);
        result.setaDouble(null);
        result.setaCharacter(null);
        return result;
    }

    private PrimitiveArrays primitivesArrays1()
    {
        final PrimitiveArrays result = new PrimitiveArrays();
        result.setBoolArray(new boolean[]{true, false, true});
        result.setByteArray(new byte[]{123, -65, 0, 1});
        result.setShortArray(new short[]{321, -876, -1});
        result.setIntArray(new int[]{12345, -12345, 0});
        result.setLongArray(new long[]{-987654321L, 987654321L, 1});
        result.setFloatArray(new float[]{123.45F, -123.45F, 0.0F});
        result.setDoubleArray(new double[]{0, -1, 1, -54321.89, 54321.89});
        result.setCharArray(new char[]{'M', 'i', 'c', 'r', 'o', 'S', 't', 'r', 'e', 'a', 'm'});
        return result;
    }

    private PrimitiveArrays primitivesArrays2()
    {
        final PrimitiveArrays result = new PrimitiveArrays();
        result.setBoolArray(null);
        result.setByteArray(null);
        result.setShortArray(null);
        result.setIntArray(null);
        result.setLongArray(null);
        result.setFloatArray(null);
        result.setDoubleArray(null);
        result.setCharArray(null);
        return result;
    }

    private PrimitiveWrapperArrays primitivesWrapperArrays1()
    {
        final PrimitiveWrapperArrays result = new PrimitiveWrapperArrays();
        result.setBoolArray(new Boolean[]{true, false, true});
        result.setByteArray(new Byte[]{123, -65, 0, 1});
        result.setShortArray(new Short[]{321, -876, -1});
        result.setIntArray(new Integer[]{12345, -12345, 0});
        result.setLongArray(new Long[]{-987654321L, 987654321L, 1L});
        result.setFloatArray(new Float[]{123.45F, -123.45F, 0.0F});
        result.setDoubleArray(new Double[]{0.0, -1.0, 1.0, -54321.89, 54321.89});
        result.setCharArray(new Character[]{'M', 'i', 'c', 'r', 'o', 'S', 't', 'r', 'e', 'a', 'm'});
        return result;
    }

    private PrimitiveWrapperArrays primitivesWrapperArrays2()
    {
        final PrimitiveWrapperArrays result = new PrimitiveWrapperArrays();
        result.setBoolArray(null);
        result.setByteArray(null);
        result.setShortArray(null);
        result.setIntArray(null);
        result.setLongArray(null);
        result.setFloatArray(null);
        result.setDoubleArray(null);
        result.setCharArray(null);
        return result;
    }

    private PrimitiveLists primitivesLists1()
    {
        final PrimitiveLists result = new PrimitiveLists();
        result.setBoolList(createList(Boolean.FALSE, Boolean.TRUE, Boolean.FALSE));
        result.setByteList(createList((byte) 123, (byte) -65, (byte) 0, (byte) 1));
        result.setShortList(createList((short) 321, (short) -876, (short) -1));
        result.setIntList(createList(12345, -12345, 0));
        result.setLongList(createList(-987654321L, 987654321L, 1L));
        result.setFloatList(createList(123.45F, -123.45F, 0.0F));
        result.setDoubleList(createList(0.0, -1.0, 1.0, -54321.89, 54321.89));
        result.setCharList(createList('M', 'i', 'c', 'r', 'o', 'S', 't', 'r', 'e', 'a', 'm'));
        return result;
    }

    private <T> List<T> createList(final T... values)
    {
        return new ArrayList<>(Arrays.asList(values));
    }

    private PrimitiveLists primitivesLists2()
    {
        final PrimitiveLists result = new PrimitiveLists();
        result.setBoolList(new ArrayList<>());
        result.setByteList(new ArrayList<>());
        result.setShortList(new ArrayList<>());
        result.setIntList(new ArrayList<>());
        result.setLongList(new ArrayList<>());
        result.setFloatList(new ArrayList<>());
        result.setDoubleList(new ArrayList<>());
        result.setCharList(new ArrayList<>());
        return result;
    }

    private PrimitiveLists primitivesLists3()
    {
        final PrimitiveLists result = new PrimitiveLists();
        result.setBoolList(createList(null, null));
        result.setByteList(createList(null, null));
        result.setShortList(createList(null, null));
        result.setIntList(createList(null, null));
        result.setLongList(createList(null, null));
        result.setFloatList(createList(null, null));
        result.setDoubleList(createList(null, null));
        result.setCharList(createList(null, null));
        return result;
    }

    private PrimitiveLists primitivesLists4()
    {
        final PrimitiveLists result = new PrimitiveLists();
        result.setBoolList(null);
        result.setByteList(null);
        result.setShortList(null);
        result.setIntList(null);
        result.setLongList(null);
        result.setFloatList(null);
        result.setDoubleList(null);
        result.setCharList(null);
        return result;
    }


    private PrimitiveSets primitivesSets1()
    {
        final PrimitiveSets result = new PrimitiveSets();
        result.setBoolSet(createSet(Boolean.FALSE, Boolean.TRUE, Boolean.FALSE));
        result.setByteSet(createSet((byte) 123, (byte) -65, (byte) 0, (byte) 1));
        result.setShortSet(createSet((short) 321, (short) -876, (short) -1));
        result.setIntSet(createSet(12345, -12345, 0));
        result.setLongSet(createSet(-987654321L, 987654321L, 1L));
        result.setFloatSet(createSet(123.45F, -123.45F, 0.0F));
        result.setDoubleSet(createSet(0.0, -1.0, 1.0, -54321.89, 54321.89));
        result.setCharSet(createSet('M', 'i', 'c', 'r', 'o', 'S', 't', 'r', 'e', 'a', 'm'));
        return result;
    }

    private <T> Set<T> createSet(final T... values)
    {
        return new HashSet<>(Arrays.asList(values));
    }

    private PrimitiveSets primitivesSets2()
    {
        final PrimitiveSets result = new PrimitiveSets();
        result.setBoolSet(new HashSet<>());
        result.setByteSet(new HashSet<>());
        result.setShortSet(new HashSet<>());
        result.setIntSet(new HashSet<>());
        result.setLongSet(new HashSet<>());
        result.setFloatSet(new HashSet<>());
        result.setDoubleSet(new HashSet<>());
        result.setCharSet(new HashSet<>());
        return result;
    }

    private PrimitiveSets primitivesSets3()
    {
        final PrimitiveSets result = new PrimitiveSets();
        result.setBoolSet(createSet(null, null));
        result.setByteSet(createSet(null, null));
        result.setShortSet(createSet(null, null));
        result.setIntSet(createSet(null, null));
        result.setLongSet(createSet(null, null));
        result.setFloatSet(createSet(null, null));
        result.setDoubleSet(createSet(null, null));
        result.setCharSet(createSet(null, null));
        return result;
    }

    private PrimitiveSets primitivesSets4()
    {
        final PrimitiveSets result = new PrimitiveSets();
        result.setBoolSet(null);
        result.setByteSet(null);
        result.setShortSet(null);
        result.setIntSet(null);
        result.setLongSet(null);
        result.setFloatSet(null);
        result.setDoubleSet(null);
        result.setCharSet(null);
        return result;
    }

    private Optionals optionals1()
    {
        final Optionals result = new Optionals();
        result.setOptBool(Optional.of(Boolean.TRUE));
        result.setOptByte(Optional.of((byte) 123));
        result.setOptShort(Optional.of((short) 321));
        result.setOptInt(Optional.of(12345));
        result.setOptLong(Optional.of(987654321L));
        result.setOptFloat(Optional.of(123.45F));
        result.setOptDouble(Optional.of(54321.89));
        result.setOptChar(Optional.of('M'));
        return result;
    }

    private Optionals optionals2()
    {
        final Optionals result = new Optionals();
        result.setOptBool(Optional.of(Boolean.FALSE));
        result.setOptByte(Optional.of((byte) -123));
        result.setOptShort(Optional.of((short) -321));
        result.setOptInt(Optional.of(-12345));
        result.setOptLong(Optional.of(-987654321L));
        result.setOptFloat(Optional.of(-123.45F));
        result.setOptDouble(Optional.of(-54321.89));
        result.setOptChar(Optional.of('m'));
        return result;
    }

    private Optionals optionals3()
    {
        final Optionals result = new Optionals();
        result.setOptBool(Optional.empty());
        result.setOptByte(Optional.empty());
        result.setOptShort(Optional.empty());
        result.setOptInt(Optional.empty());
        result.setOptLong(Optional.empty());
        result.setOptFloat(Optional.empty());
        result.setOptDouble(Optional.empty());
        result.setOptChar(Optional.empty());
        return result;
    }

    private Optionals optionals4()
    {
        final Optionals result = new Optionals();
        result.setOptBool(null);
        result.setOptByte(null);
        result.setOptShort(null);
        result.setOptInt(null);
        result.setOptLong(null);
        result.setOptFloat(null);
        result.setOptDouble(null);
        result.setOptChar(null);
        return result;
    }

    private Strings strings1()
    {
        final Strings result = new Strings();
        result.setValue("MicroStream");
        result.setOptValue(Optional.of("MicroStream as optional"));
        result.setStringArray(new String[]{"Array", "Of", "String"});
        result.setStringList(createList("List", "Of", "String"));
        result.setStringSet(createSet("Set", "Of", "String"));
        return result;
    }

    private Strings strings2()
    {
        final Strings result = new Strings();
        result.setValue("");
        result.setOptValue(Optional.empty());
        result.setStringArray(new String[0]);
        result.setStringList(new ArrayList<>());
        result.setStringSet(new HashSet<>());
        return result;
    }

    private Strings strings3()
    {
        final Strings result = new Strings();
        result.setValue(null);
        result.setOptValue(null);
        result.setStringArray(null);
        result.setStringList(null);
        result.setStringSet(null);
        return result;
    }

    private Enums enums1()
    {
        final Enums result = new Enums();
        result.setAnEnum(MyEnum.ENUM_VAL2);
        result.setEnumArray(new MyEnum[]{MyEnum.ENUM_VAL1, MyEnum.ENUM_VAL3});
        result.setEnumOpt(Optional.of(MyEnum.ENUM_VAL3));
        result.setEnumList(createList(MyEnum.ENUM_VAL2, MyEnum.ENUM_VAL1));
        result.setEnumSet(createSet(MyEnum.ENUM_VAL1, MyEnum.ENUM_VAL3));
        return result;
    }

    private Enums enums2()
    {
        final Enums result = new Enums();
        result.setAnEnum(null);
        result.setEnumArray(new MyEnum[0]);
        result.setEnumOpt(Optional.empty());
        result.setEnumList(new ArrayList<>());
        result.setEnumSet(new HashSet<>());
        return result;
    }

    private Enums enums3()
    {
        final Enums result = new Enums();
        result.setAnEnum(null);
        result.setEnumArray(null);
        result.setEnumOpt(null);
        result.setEnumList(null);
        result.setEnumSet(null);
        return result;
    }

    private BigValues bigValues1()
    {
        final BigValues result = new BigValues();
        result.setaBigInteger(BigInteger.valueOf(123456789L));
        result.setaBigDecimal(BigDecimal.valueOf(123456789.54321));
        result.setBigIntegerArray(new BigInteger[]{BigInteger.ONE, BigInteger.valueOf(-12345L)});
        result.setBigDecimalArray(new BigDecimal[]{BigDecimal.valueOf(-6789.21), BigDecimal.TEN});
        result.setBigIntegerOpt(Optional.of(BigInteger.valueOf(987654321L)));
        result.setBigDecimalOpt(Optional.of(BigDecimal.valueOf(987654321.12345)));
        result.setBigIntegerList(createList(BigInteger.valueOf(-123456789L), BigInteger.ZERO));
        result.setBigDecimalList(createList(BigDecimal.valueOf(123456789.54321), BigDecimal.ZERO));
        result.setBigIntegerSet(createSet(BigInteger.valueOf(12345L), BigInteger.valueOf(-123)));
        result.setBigDecimalSet(createSet(BigDecimal.valueOf(-987654321.12345), BigDecimal.valueOf(3.5)));
        return result;

    }

    private BigValues bigValues2()
    {
        final BigValues result = new BigValues();
        result.setaBigInteger(null);
        result.setaBigDecimal(null);
        result.setBigIntegerArray(new BigInteger[0]);
        result.setBigDecimalArray(new BigDecimal[0]);
        result.setBigIntegerOpt(Optional.empty());
        result.setBigDecimalOpt(Optional.empty());
        result.setBigIntegerList(new ArrayList<>());
        result.setBigDecimalList(new ArrayList<>());
        result.setBigIntegerSet(new HashSet<>());
        result.setBigDecimalSet(new HashSet<>());
        return result;

    }

    private BigValues bigValues3()
    {
        final BigValues result = new BigValues();
        result.setaBigInteger(null);
        result.setaBigDecimal(null);
        result.setBigIntegerArray(null);
        result.setBigDecimalArray(null);
        result.setBigIntegerOpt(null);
        result.setBigDecimalOpt(null);
        result.setBigIntegerList(null);
        result.setBigDecimalList(null);
        result.setBigIntegerSet(null);
        result.setBigDecimalSet(null);
        return result;

    }

    private Dates dates1()
    {
        final Dates result = new Dates();
        result.setaDate(new Date());
        result.setaLocalDate(LocalDate.now());
        result.setaLocalDateTime(LocalDateTime.now());
        result.setAnInstant(Instant.now());
        result.setaTimestamp(new Timestamp(new Date().getTime()));
        return result;
    }

    private Dates dates2()
    {
        final Dates result = new Dates();
        result.setaDate(null);
        result.setaLocalDate(null);
        result.setaLocalDateTime(null);
        result.setAnInstant(null);
        result.setaTimestamp(null);
        return result;
    }

    private Maps maps1()
    {
        final Maps result = new Maps();

        Map<String, String> m1 = new HashMap<>();
        addEntry(m1, "key", "value");
        addEntry(m1, "foo", "bar");
        result.setStringMap(m1);

        Map<String, Long> m2 = new HashMap<>();
        addEntry(m2, "item1", 123L);
        addEntry(m2, "item2", 87L);
        addEntry(m2, "item3", 5432L);
        result.setCountMap(m2);

        Map<Double, Double> m3 = new HashMap<>();
        addEntry(m3, 1.0, 1.0);
        addEntry(m3, 2.0, 4.0);
        addEntry(m3, 3.0, 9.0);
        addEntry(m3, 4.0, 16.0);
        result.setDataMap(m3);

        return result;
    }

    private <K, V> void addEntry(Map<K, V> map, K key, V value)
    {
        map.put(key, value);
    }

    private Maps maps2()
    {
        final Maps result = new Maps();

        result.setStringMap(new HashMap<>());
        result.setCountMap(new HashMap<>());
        result.setDataMap(new HashMap<>());

        return result;
    }

    private Maps maps3()
    {
        final Maps result = new Maps();

        result.setStringMap(null);
        result.setCountMap(null);
        result.setDataMap(null);

        return result;
    }
}
